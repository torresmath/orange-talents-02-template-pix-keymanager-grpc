package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*

internal class PixKeyTest {

    @Test
    fun `should format CPF`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setKeyIdentifier("425.497.898-73")
            .build()

        assertEquals("42549789873", keyRequest.toPixKey().keyIdentifier)
    }


    companion object {
        @JvmStatic
        fun params() = listOf(
            "",
            UUID.randomUUID().toString(),
            "425.497.898-73",
            "+5511972651418",
            "email@test.com"
        )
    }

    @ParameterizedTest
    @MethodSource("params")
    fun `should generate UUID for key identifier`(identifier: String) {
        val keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.RANDOM)
            .setKeyIdentifier(identifier)
            .build()

        assertNotEquals(keyRequest.toPixKey(), identifier)
    }
}