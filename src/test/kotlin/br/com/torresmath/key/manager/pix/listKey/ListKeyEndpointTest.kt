package br.com.torresmath.key.manager.pix.listKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.ListKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.ListKeyRequest
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import javax.inject.Inject

@MicronautTest
internal class ListKeyEndpointTest(
    @Inject val blockingStub: ListKeyGrpcServiceGrpc.ListKeyGrpcServiceBlockingStub,
    @Inject val repository: PixKeyRepository
) {

    @BeforeEach
    internal fun setUp() {
        val keys = mutableListOf(
            PixKey(clientId, KeyType.CPF, "42549789873", AccountType.CHECKING_ACCOUNT),
            PixKey(clientId, KeyType.MOBILE_NUMBER, "+5511972651418", AccountType.CHECKING_ACCOUNT),
            PixKey(clientId, KeyType.EMAIL, "email@test.com", AccountType.CHECKING_ACCOUNT)
        )
        repository.saveAll(keys)
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `should return keys list`() {

        val request = ListKeyRequest.newBuilder()
            .setClientId(clientId)
            .build()

        val response = blockingStub.listKeyByClientId(request)
        assertEquals(3, response.keysCount)
    }

    @Test
    fun `should return empty list`() {
        val request = ListKeyRequest.newBuilder()
            .setClientId(UUID.randomUUID().toString())
            .build()

        val response = blockingStub.listKeyByClientId(request)
        assertTrue(response.keysList.isEmpty())
    }

    companion object {

        val clientId: String = UUID.randomUUID().toString()

        @JvmStatic
        fun params() = listOf(
            "",
            clientId + "1",
            "value"
        )
    }

    @ParameterizedTest
    @MethodSource("params")
    fun `should return ILLEGAL_ARGUMENT Exception`(clientId: String) {

        val request = ListKeyRequest.newBuilder()
            .setClientId(clientId)
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.listKeyByClientId(request) }
        assertEquals(Status.Code.INVALID_ARGUMENT, exc.status.code)
    }
}

@Factory
private class ListKeyClient {

    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            ListKeyGrpcServiceGrpc.ListKeyGrpcServiceBlockingStub {
        return ListKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}