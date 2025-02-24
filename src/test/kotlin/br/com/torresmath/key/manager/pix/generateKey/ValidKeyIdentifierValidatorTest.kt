package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.annotations.ValidKeyIdentifierValidator
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import javax.inject.Inject
import javax.validation.ConstraintValidatorContext

@MicronautTest
internal class ValidKeyIdentifierValidatorTest(@Inject val validator: ValidKeyIdentifierValidator) {


    companion object {
        @JvmStatic
        fun mobileParams() = listOf(
            Arguments.of("+55972651418", true),
            Arguments.of("+11000000000", true),
            Arguments.of("+111111111111", true),
            Arguments.of("+1234567891234", true),
            Arguments.of("+12345678", true),
            Arguments.of("+01000000000", false),
            Arguments.of("+01000000000", false),
            Arguments.of("55972651418", false),
        )

        @JvmStatic
        fun randomParams() = listOf(
            Arguments.of("22ccc326-995f-11eb-a8b3-0242ac130003", PixKeyStatus.ACTIVE, true),
            Arguments.of("value", PixKeyStatus.ACTIVE, false),
            Arguments.of("", PixKeyStatus.ACTIVE, false),
            Arguments.of("22ccc326-995f-11eb-a8b3-0242ac130003", PixKeyStatus.INACTIVE, false),
            Arguments.of("value", PixKeyStatus.INACTIVE, false),
            Arguments.of("", PixKeyStatus.INACTIVE, true),
        )

        @JvmStatic
        fun cpfParams() = listOf(
            Arguments.of("42549789873", true),
            Arguments.of("value", false),
        )

        @JvmStatic
        fun emailParams() = listOf(
            Arguments.of("email@test.com", true),
            Arguments.of("email@test.com.br", true),
            Arguments.of("email@test", true),
            Arguments.of("email", false),
            Arguments.of("email@", false)
        )
    }

    @ParameterizedTest
    @MethodSource("mobileParams")
    fun `Valid key identifiers for Key Type == Mobiler Number`(identifier: String, expected: Boolean) {

        val mock = Mockito.mock(ConstraintValidatorContext::class.java)

        val pix = PixKey("", KeyType.MOBILE_NUMBER, identifier, AccountType.CHECKING_ACCOUNT)
        assertEquals(expected, validator.isValid(pix, mock))
    }

    @ParameterizedTest
    @MethodSource("randomParams")
    fun `Valid key identifiers for Type == RANDOM`(identifier: String, status: PixKeyStatus, expected: Boolean) {

        val mock = Mockito.mock(ConstraintValidatorContext::class.java)

        val pix = PixKey("", KeyType.RANDOM, identifier, AccountType.CHECKING_ACCOUNT)
        pix.status = status
        assertEquals(expected, validator.isValid(pix, mock))
    }

    @ParameterizedTest
    @MethodSource("cpfParams")
    fun `Valid key identifiers for Type == CPF`(identifier: String, expected: Boolean) {

        val mock = Mockito.mock(ConstraintValidatorContext::class.java)

        val pix = PixKey("", KeyType.CPF, identifier, AccountType.CHECKING_ACCOUNT)
        assertEquals(expected, validator.isValid(pix, mock))
    }

    @ParameterizedTest
    @MethodSource("emailParams")
    fun `Valid key identifiers for Type == Email`(identifier: String, expected: Boolean) {

        val mock = Mockito.mock(ConstraintValidatorContext::class.java)

        val pix = PixKey(
            clientId = "",
            keyType = KeyType.EMAIL,
            keyIdentifier = identifier,
            accountType = AccountType.CHECKING_ACCOUNT
        )
        assertEquals(expected, validator.isValid(pix, mock))
    }

    @Test
    fun `should return false if null entry`() {
        assertEquals(false, validator.isValid(null, Mockito.mock(ConstraintValidatorContext::class.java)))
    }

    @Test
    fun `should return false with invalid KeyType`() {

        val pix = PixKey(
            clientId = "",
            keyType = KeyType.UNRECOGNIZED,
            keyIdentifier = "",
            accountType = AccountType.CHECKING_ACCOUNT
        )

        assertEquals(false, validator.isValid(pix, Mockito.mock(ConstraintValidatorContext::class.java)))
    }
}