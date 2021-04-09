package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import org.hibernate.validator.constraints.br.CPF
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Validated
@Introspected
class PixKey(
    @field:NotNull(message = "Customer/Client ID cannot be null")
    @field:NotBlank(message = "Customer/Client ID cannot be blank")
    val clientId: String,

    @field:NotNull(message = "PIX Key Type cannot be null")
    val keyType: KeyType,

    @field:NotNull(message = "PIX Key Identifier cannot be null")
    @field:Size(max = 77, message = "PIX Key Identifier's length can't be greater than 77")
    val keyIdentifier: String,

    @field:NotNull(message = "Customer/Client account type cannot be null")
    val accountType: AccountType,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    val createdDate: LocalDateTime = LocalDateTime.now()

    fun isValidIdentifier(validator: Validator): Boolean {

        return when (keyType) {
            KeyType.RANDOM -> keyIdentifier.isBlank()
            KeyType.MOBILE_NUMBER -> "^\\+[1-9][0-9]\\d{1,14}\$".toRegex().matches(keyIdentifier)
            KeyType.CPF -> validator.validate(ValidCPF(keyIdentifier)).isEmpty()
            KeyType.EMAIL -> validator.validate(ValidEmail(keyIdentifier)).isEmpty()
            else -> false
        }
    }

    private class ValidCPF(@field:CPF val identifier: String)
    private class ValidEmail(@field:Email val identifier: String)
}

fun KeyRequest.toPixKey(): PixKey {
    return PixKey(clientId = this.clientId, keyType = this.keyType, keyIdentifier = this.keyIdentifier, accountType = this.accountType)
}