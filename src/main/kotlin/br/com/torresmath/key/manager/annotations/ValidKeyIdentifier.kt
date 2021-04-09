package br.com.torresmath.key.manager.annotations

import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.generateKey.PixKey
import io.micronaut.validation.validator.Validator
import org.hibernate.validator.constraints.br.CPF
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import javax.validation.constraints.Email
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidKeyIdentifierValidator::class])
annotation class ValidKeyIdentifier(
    val message: String = "Invalid pix key identifier for provided pix key type",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidKeyIdentifierValidator : ConstraintValidator<ValidKeyIdentifier, PixKey> {

    @Inject
    lateinit var validator: Validator

    override fun isValid(value: PixKey?, context: ConstraintValidatorContext?): Boolean {
        if (value == null)
            return false

        return when (value.keyType) {
            KeyType.RANDOM -> kotlin.runCatching { UUID.fromString(value.keyIdentifier) }.isSuccess
            KeyType.MOBILE_NUMBER -> "^\\+[1-9][0-9]\\d{1,14}\$".toRegex().matches(value.keyIdentifier)
            KeyType.CPF -> validator.validate(ValidCPF(value.keyIdentifier)).isEmpty()
            KeyType.EMAIL -> validator.validate(ValidEmail(value.keyIdentifier)).isEmpty()
            else -> false
        }

    }

    private class ValidCPF(@field:CPF val identifier: String)
    private class ValidEmail(@field:Email val identifier: String)

}
