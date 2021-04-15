package br.com.torresmath.key.manager.annotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import javax.validation.ConstraintValidatorContext

internal class ValidUUIDValidatorTest {

    companion object {
        @JvmStatic
        fun params() = listOf(
            Arguments.of("c56dfef4-7901-44fb-84e2-a2cefb157890", true),
            Arguments.of("c56dfef4790144fb84e2a2cefb157890", false),
            Arguments.of("c56dfef4-7901-44fb-84e2-a2cefb1578901", false),
            Arguments.of("value", false),
            Arguments.of("12345678910", false),
            Arguments.of("", false),
            Arguments.of(null, false)
        )
    }

    @ParameterizedTest
    @MethodSource("params")
    fun `test valid UUIDs`(str: String?, expected: Boolean) {

        val mockContext = Mockito.mock(ConstraintValidatorContext::class.java)

        assertEquals(expected, ValidUUIDValidator().isValid(str, mockContext))
    }
}