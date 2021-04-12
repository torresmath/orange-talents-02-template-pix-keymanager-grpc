package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.annotations.ValidKeyIdentifier
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
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

    @field:NotNull(message = "PIX Key Type cannot be null")
    val keyType: KeyType,

    @field:NotNull(message = "PIX Key Identifier cannot be null")
    @field:Size(max = 77, message = "PIX Key Identifier's length can't be greater than 77")
    var keyIdentifier: String,

    @field:NotNull(message = "Customer/Client account type cannot be null")
    val accountType: AccountType,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    val createdDate: LocalDateTime = LocalDateTime.now()

    val pixUuid: String = UUID.randomUUID().toString()

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
            this.keyIdentifier = UUID.randomUUID().toString()

        if (this.keyType == KeyType.CPF)
            this.keyIdentifier = this.keyIdentifier.replace("[.-]".toRegex(), "")

    }
}