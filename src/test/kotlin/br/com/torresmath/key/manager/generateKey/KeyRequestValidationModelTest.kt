package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import javax.inject.Inject

@MicronautTest
internal class KeyRequestValidationModelTest {

    @Inject
    lateinit var validator: Validator

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
            Arguments.of("", true),
            Arguments.of("value", false),
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

        val req = KeyRequest.newBuilder()
            .setKeyIdentifier(identifier)
            .setKeyType(KeyType.MOBILE_NUMBER)
            .build()

        val keyRequestModel = KeyRequestValidationModel(req)
        assertEquals(expected, keyRequestModel.isValidIdentifier(validator))
    }

    @ParameterizedTest
    @MethodSource("randomParams")
    fun `Valid key identifiers for Type == RANDOM`(identifier: String?, expected: Boolean) {
        val req = KeyRequest.newBuilder()
            .setKeyIdentifier(identifier)
            .setKeyType(KeyType.RANDOM)
            .build()

        val keyRequestModel = KeyRequestValidationModel(req)
        assertEquals(expected, keyRequestModel.isValidIdentifier(validator))
    }

    @ParameterizedTest
    @MethodSource("cpfParams")
    fun `Valid key identifiers for Type == CPF`(identifier: String?, expected: Boolean) {
        val req = KeyRequest.newBuilder()
            .setKeyIdentifier(identifier)
            .setKeyType(KeyType.CPF)
            .build()

        val keyRequestModel = KeyRequestValidationModel(req)
        assertEquals(expected, keyRequestModel.isValidIdentifier(validator))
    }

    @ParameterizedTest
    @MethodSource("emailParams")
    fun `Valid key identifiers for Type == Email`(identifier: String?, expected: Boolean) {
        val req = KeyRequest.newBuilder()
            .setKeyIdentifier(identifier)
            .setKeyType(KeyType.EMAIL)
            .build()

        val keyRequestModel = KeyRequestValidationModel(req)
        assertEquals(expected, keyRequestModel.isValidIdentifier(validator))
    }
}