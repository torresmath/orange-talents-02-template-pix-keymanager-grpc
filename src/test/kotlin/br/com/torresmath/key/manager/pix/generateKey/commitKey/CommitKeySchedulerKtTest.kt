package br.com.torresmath.key.manager.pix.generateKey.commitKey

import br.com.torresmath.key.manager.AccountType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CommitKeySchedulerKtTest {

    companion object {
        @JvmStatic
        fun params() = listOf(
            Arguments.of(AccountType.CHECKING_ACCOUNT, "CONTA_CORRENTE"),
            Arguments.of(AccountType.SAVINGS_ACCOUNT, "CONTA_POUPANCA"),
            Arguments.of(AccountType.UNRECOGNIZED, "")
        )
    }

    @ParameterizedTest
    @MethodSource("params")
    fun `test toErpItauValue`(accountType: AccountType, expected: String) {
        assertEquals(expected, accountType.toErpItauValue())
    }

}