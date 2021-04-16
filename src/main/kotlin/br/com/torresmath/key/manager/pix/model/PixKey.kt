package br.com.torresmath.key.manager.pix.model

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.annotations.ValidKeyIdentifier
import br.com.torresmath.key.manager.annotations.ValidUUID
import br.com.torresmath.key.manager.pix.generateKey.ErpItauAccount
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbClient
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbCreatePixKeyRequest
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbDeletePixKeyRequest
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbOwner
import br.com.torresmath.key.manager.pix.generateKey.toBcbBankAccountRequest
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
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
    @field:ValidUUID
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

    @Embedded
    var account: Account? = null

    fun markAsToDelete(repository: PixKeyRepository) {
        this.status = PixKeyStatus.DELETE
        repository.update(this)
    }

    fun commitDeletion(
        bcbDeleteRequest: BcbDeletePixKeyRequest,
        bcbClient: BcbClient,
        pixRepositoryImpl: PixRepositoryImpl
    ) {
        kotlin.runCatching { bcbClient.deletePixKey(this.keyIdentifier, bcbDeleteRequest) }
            .onSuccess {
                if (it.status.code == 404)
                    println("WARNING - Key $pixUuid Apparently doesn't exists at BCB. Deleting local reference anyway")

                pixRepositoryImpl.delete(this)
            }
            .onFailure {
                when (it) {
                    is HttpClientResponseException -> {
                        if (it.status.code == 403) {
                            println("WARNING - Key $pixUuid already linked to another participant at BCB. Deleting local reference anyway")
                            pixRepositoryImpl.delete(this)
                        }
                    }
                    else -> throw it
                }
            }
    }

    fun commit(
        itauAccount: ErpItauAccount,
        bcbClient: BcbClient,
        repositoryImpl: PixRepositoryImpl
    ) {

        val request: BcbCreatePixKeyRequest = buildBcbCreatePixRequest(itauAccount)

        kotlin.runCatching { bcbClient.generatePixKey(request) }
            .onSuccess {
                this.status = PixKeyStatus.ACTIVE
                repositoryImpl.update(this)
            }.onFailure {
                when (it) {
                    is HttpClientResponseException -> {
                        this.status = PixKeyStatus.FAILED
                        repositoryImpl.update(this)
                    }
                    else -> throw it
                }
            }
    }

    protected fun buildBcbCreatePixRequest(itauAccount: ErpItauAccount): BcbCreatePixKeyRequest {
        return BcbCreatePixKeyRequest(
            keyType = this.keyType.toBcbKeyType(),
            key = this.keyIdentifier,
            bankAccount = itauAccount.toBcbBankAccountRequest(),
            owner = BcbOwner(
                type = BcbOwner.BcbOwnerType.NATURAL_PERSON,
                name = itauAccount.titular.nome,
                taxIdNumber = itauAccount.titular.cpf
            )
        )
    }

    override fun toString(): String {
        return "PixKey(clientId='$clientId', keyType=$keyType, keyIdentifier='$keyIdentifier', accountType=$accountType)"
    }

    fun save(account: Account, pixKeyRepository: PixKeyRepository) {
        this.account = account
        pixKeyRepository.save(this)
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