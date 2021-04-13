package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.annotations.ValidKeyIdentifier
import br.com.torresmath.key.manager.pix.generateKey.commitKey.*
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Validated
@Introspected
@ValidKeyIdentifier
class PixKey(
    @field:NotNull(message = "Customer/Client ID cannot be null")
    @field:NotBlank(message = "Customer/Client ID cannot be blank")
    val clientId: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull(message = "PIX Key Type cannot be null")
    val keyType: KeyType,

    @field:NotNull(message = "PIX Key Identifier cannot be null")
    @field:Size(max = 77, message = "PIX Key Identifier's length can't be greater than 77")
    var keyIdentifier: String,

    @Enumerated(EnumType.STRING)
    @field:NotNull(message = "Customer/Client account type cannot be null")
    val accountType: AccountType,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @NotNull
    val createdDate: LocalDateTime = LocalDateTime.now()

    @NotNull
    val pixUuid: String = UUID.randomUUID().toString()

    @NotNull
    @Enumerated(EnumType.STRING)
    var status: PixKeyStatus = PixKeyStatus.INACTIVE

    @Transactional
    fun submitKey(
        itauAccount: ErpItauAccount,
        bcbClient: BcbClient,
        repository: InactivePixRepository
    ) {
        val request = BcbCreatePixKeyRequest(
            this.keyType.toBcbKeyType(),
            this.keyIdentifier,
            itauAccount.toBcbBankAccountRequest(),
            BcbCreatePixKeyRequest.BcbOwnerRequest(
                BcbCreatePixKeyRequest.BcbOwnerRequest.BcbOwnerType.NATURAL_PERSON,
                itauAccount.titular.nome,
                itauAccount.titular.cpf
            )
        )

        bcbClient.generatePixKey(request).let {
            this.status = PixKeyStatus.ACTIVE
            repository.update(this)
        }
    }

    override fun toString(): String {
        return "PixKey(clientId='$clientId', keyType=$keyType, keyIdentifier='$keyIdentifier', accountType=$accountType)"
    }

}

fun KeyRequest.toPixKey(): PixKey {
    return PixKey(
        clientId = this.clientId,
        keyType = this.keyType,
        keyIdentifier = this.keyIdentifier,
        accountType = this.accountType
    ).apply {
        if (this.keyType == KeyType.RANDOM)
            this.keyIdentifier = ""

        if (this.keyType == KeyType.CPF)
            this.keyIdentifier = this.keyIdentifier.replace("[.-]".toRegex(), "")

    }
}

private fun KeyType.toBcbKeyType(): String {
    return when (this) {
        KeyType.MOBILE_NUMBER -> "PHONE"
        else -> this.name
    }
}