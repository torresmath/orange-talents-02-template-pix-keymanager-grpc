package br.com.torresmath.key.manager.pix.retrieveKey

import br.com.torresmath.key.manager.*
import br.com.torresmath.key.manager.pix.model.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import javax.inject.Inject

@MicronautTest
internal class RetrieveKeyEndpointTest(
    @Inject val repository: PixKeyRepository,
    @Inject val blockingStub: RetrieveKeyGrpcServiceGrpc.RetrieveKeyGrpcServiceBlockingStub
) {

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
}

@Factory
private class RetrieveKeyClient() {
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
            : RetrieveKeyGrpcServiceGrpc.RetrieveKeyGrpcServiceBlockingStub {
        return RetrieveKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}