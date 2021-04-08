package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.annotations.ValidEnum
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import org.hibernate.validator.constraints.br.CPF
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Validated
@Introspected
class KeyRequestValidationModel(keyRequest: KeyRequest) {

    @field:NotNull
    @field:NotBlank
    val clientId: String = keyRequest.clientId

    @field:NotNull
    val keyType: KeyType = keyRequest.keyType

    @field:NotNull
    @field:Size(max = 77)
    val keyIdentifier: String = keyRequest.keyIdentifier

    @field:NotNull
    @field:ValidEnum(targetEnum = AccountType::class)
    val accountType: AccountType = keyRequest.accountType

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

fun KeyRequest.toValidationModel(): KeyRequestValidationModel {
    return KeyRequestValidationModel(this)
}