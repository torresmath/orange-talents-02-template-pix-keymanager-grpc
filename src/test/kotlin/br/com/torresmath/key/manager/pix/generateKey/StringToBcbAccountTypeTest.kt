package br.com.torresmath.key.manager.pix.generateKey

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class StringToBcbAccountTypeTest {

    @Test
    fun `should convert stringToBcbAccountType`() {

        assertAll(
            { assertDoesNotThrow { stringToBcbAccountType("CONTA_CORRENTE") } },
            { assertDoesNotThrow { stringToBcbAccountType("CONTA_POUPANCA") } },
            { assertThrows<IllegalStateException> { stringToBcbAccountType("") } },
            { assertThrows<IllegalStateException> { stringToBcbAccountType("conta_corrente") } },
            { assertThrows<IllegalStateException> { stringToBcbAccountType("conta_poupanca") } }
        )
    }
}