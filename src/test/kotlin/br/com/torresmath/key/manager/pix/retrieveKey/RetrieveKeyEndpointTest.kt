package br.com.torresmath.key.manager.pix.retrieveKey

import br.com.torresmath.key.manager.*
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbBankAccount
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbClient
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbOwner
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbPixKeyResponse
import br.com.torresmath.key.manager.pix.model.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest
internal class RetrieveKeyEndpointTest(
    @Inject val repository: PixKeyRepository,
    @Inject val blockingStub: RetrieveKeyGrpcServiceGrpc.RetrieveKeyGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var bcbMock: BcbClient

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @BeforeEach
    internal fun setUp() {
        pixKey.account = Account(
            "0001",
            "291900",
            AccountOwner("usuario teste", "42549789873")
        )

        repository.update(pixKey)
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `should return response`() {

        val req = RetrieveKeyRequest.newBuilder()
            .setClientId(pixKey.clientId)
            .setPixId(pixKey.pixUuid)
            .build()

        assertDoesNotThrow { blockingStub.retrieveKey(req) }
    }

    companion object {

        var defaultRequest: KeyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setKeyIdentifier("42549789873")
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .build()

        var pixKey: PixKey = defaultRequest.toPixKey()

        @JvmStatic
        fun params() = listOf(
            Arguments.of(UUID.randomUUID().toString(), pixKey.pixUuid),
            Arguments.of(pixKey.clientId, UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )

        @JvmStatic
        fun invalidParams() =
            listOf("", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    }

    @ParameterizedTest
    @MethodSource("params")
    fun `should return NOT_FOUND`(clientId: String, pixId: String) {
        val req = RetrieveKeyRequest.newBuilder()
            .setClientId(clientId)
            .setPixId(pixId)
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.retrieveKey(req) }
        assertEquals(Status.Code.NOT_FOUND, exc.status.code)
    }

    @ParameterizedTest
    @MethodSource("invalidParams")
    fun `should return INVALID_ARGUMENT Retrieve Key By Identifier`(key: String) {

        val request = RetrieveKeyByIdentifierRequest.newBuilder()
            .setKey(key)
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.retrieveKeyByIdentifier(request) }
        assertEquals(Status.Code.INVALID_ARGUMENT, exc.status.code)
    }

    @Test
    fun `should return pixKey By Identifier`() {

        val request = RetrieveKeyByIdentifierRequest.newBuilder()
            .setKey(pixKey.keyIdentifier)
            .build()

        val response = blockingStub.retrieveKeyByIdentifier(request)
        assertEquals(pixKey.clientId, response.clientId)
        assertEquals(pixKey.pixUuid, response.pixId)
        verifyZeroInteractions(bcbMock)
    }
    
    @Test
    fun `should return pixKey By Identifier from BCB`() {
        val key = "+5511972651418"
        val request = RetrieveKeyByIdentifierRequest.newBuilder()
            .setKey(key)
            .build()

        val bcbResponse = BcbPixKeyResponse(
            "PHONE",
            key,
            BcbBankAccount("60701190", "0001", "291900", BcbBankAccount.BcbAccountType.CACC),
            BcbOwner(BcbOwner.BcbOwnerType.NATURAL_PERSON, "test user", "42549789873"),
            LocalDateTime.now().toString()
        )

        Mockito.`when`(bcbMock.getPixKey(key))
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = blockingStub.retrieveKeyByIdentifier(request)
        assertEquals("", response.clientId)
        assertEquals("", response.pixId)
        assertEquals(bcbResponse.bankAccount.accountNumber, response.account.number)
        verify(bcbMock, times(1)).getPixKey(key)

    }

    @Test
    fun `should return NOT_FOUND find by Identifier`() {
        val key = "+5511972651418"
        val request = RetrieveKeyByIdentifierRequest.newBuilder()
            .setKey(key)
            .build()

        val bcbResponse = BcbPixKeyResponse(
            "PHONE",
            key,
            BcbBankAccount("60701190", "0001", "291900", BcbBankAccount.BcbAccountType.CACC),
            BcbOwner(BcbOwner.BcbOwnerType.NATURAL_PERSON, "test user", "42549789873"),
            LocalDateTime.now().toString()
        )

        Mockito.`when`(bcbMock.getPixKey(key))
            .thenReturn(HttpResponse.notFound())

        val exc = assertThrows<StatusRuntimeException> { blockingStub.retrieveKeyByIdentifier(request) }
        assertEquals(Status.Code.NOT_FOUND, exc.status.code)
        verify(bcbMock, times(1)).getPixKey(key)
    }
}

@Factory
private class RetrieveKeyClient() {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
            : RetrieveKeyGrpcServiceGrpc.RetrieveKeyGrpcServiceBlockingStub {
        return RetrieveKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}