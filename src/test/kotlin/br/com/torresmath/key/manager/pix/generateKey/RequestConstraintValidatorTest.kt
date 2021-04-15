package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.model.PixKey
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.validation.ConstraintViolationException

@MicronautTest
internal class RequestConstraintValidatorTest(
    @field:Inject
    val constraintValidator: RequestConstraintValidator
) {

    @Inject
    lateinit var validator: Validator

    @Test
    fun `should throw ConstaintViolationException`() {

        val pixKey = PixKey("", KeyType.CPF, "", AccountType.CHECKING_ACCOUNT)

        assertThrows<ConstraintViolationException> { constraintValidator.validate(pixKey) }
    }

    @Test
    fun `should not throw exception`() {
        val pixKey = PixKey("232ddbc6-9b9d-11eb-a8b3-0242ac130003",
            KeyType.CPF, "42549789873",
            AccountType.CHECKING_ACCOUNT
        )

        assertDoesNotThrow { constraintValidator.validate(pixKey) }
    }
}