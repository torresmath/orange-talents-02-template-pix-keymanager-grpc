package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.generateKey.commitKey.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.exceptions.ReadTimeoutException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest
internal class PixKeyTest(
    @field:Inject
    val repository: InactivePixRepository
) {

    @field:Inject lateinit var bcbMock: BcbClient

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

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

    var defaultRequest: KeyRequest = KeyRequest.newBuilder()
        .setKeyType(KeyType.CPF)
        .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
        .setKeyIdentifier("42549789873")
        .setAccountType(AccountType.CHECKING_ACCOUNT)
        .build()

    var pixKey: PixKey = defaultRequest.toPixKey()
    var itauAccount: ErpItauAccount = ErpItauAccount(
        "CONTA_CORRENTE",
        ErpItauInstitution("ITAÚ UNIBANCO S.A.", "60701190"),
        "0001",
        "291900",
        ErpItauCustomer(pixKey.clientId, "User test", pixKey.keyIdentifier)
    )

    var bcbRequest = BcbCreatePixKeyRequest(
        keyType = "CPF",
        key = pixKey.keyIdentifier,
        bankAccount = itauAccount.toBcbBankAccountRequest(),
        owner = BcbOwner(
            type = BcbOwner.BcbOwnerType.NATURAL_PERSON,
            name = itauAccount.titular.nome,
            taxIdNumber = itauAccount.titular.cpf
        )
    )

    @Test
    fun `should set key status as ACTIVE`() {

        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenReturn(
                BcbCreatePixKeyResponse(
                    bcbRequest.keyType,
                    bcbRequest.key,
                    bcbRequest.bankAccount,
                    bcbRequest.owner,
                    LocalDateTime.now().toString()
                )
            )

        pixKey.commit(itauAccount, bcbMock, repository)
        assertEquals(PixKeyStatus.ACTIVE, pixKey.status)
    }

    @Test
    fun `should set key status as FAILED`() {
        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenThrow(HttpClientResponseException("UNPROCESSABLE_ENTITY", HttpResponse.unprocessableEntity<Any>()))

        pixKey.commit(itauAccount, bcbMock, repository)
        assertEquals(PixKeyStatus.FAILED, pixKey.status)
    }

    @Test
    fun `should throw exception when service is down`() {
        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenThrow(ReadTimeoutException.TIMEOUT_EXCEPTION)

        assertThrows<ReadTimeoutException> { pixKey.commit(itauAccount, bcbMock, repository) }
        assertEquals(PixKeyStatus.INACTIVE, pixKey.status)
    }
}