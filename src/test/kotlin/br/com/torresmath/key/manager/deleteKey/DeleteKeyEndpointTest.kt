package br.com.torresmath.key.manager.deleteKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.DeleteKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.DeleteKeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.generateKey.PixKey
import br.com.torresmath.key.manager.generateKey.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.inject.Inject

@MicronautTest
internal class DeleteKeyEndpointTest(
    @field:Inject
    val blockingStub: DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var pixKeyRepository: PixKeyRepository

    val defaultPixKey =
        PixKey("c56dfef4-7901-44fb-84e2-a2cefb157890", KeyType.CPF, "42549789873", AccountType.CHECKING_ACCOUNT)

    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.save(defaultPixKey)
    }

    @AfterEach
    internal fun tearDown() = pixKeyRepository.deleteAll()

    @Test
    fun `should return NOT_FOUND`() {

        val request = DeleteKeyRequest.newBuilder()
            .setClientId("invalid")
            .setPixId("id")
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.deleteKey(request) }
        assertEquals(Status.NOT_FOUND.code, exc.status.code)
    }
}

@MicronautTest
class DeleteKeyEndpointUnitTest(
    @field:Inject
    val blockingStub: DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceBlockingStub,
    @field:Inject
    val mockRepository: PixKeyRepository
) {

    @MockBean(PixKeyRepository::class)
    fun mockRepository(): PixKeyRepository {
        return mock(PixKeyRepository::class.java)
    }

    @Test
    fun `should return INTERNAL`() {

        val request = DeleteKeyRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setPixId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .build()

        `when`(
            mockRepository.findByClientIdAndPixUuid(request.clientId, request.pixId)
        ).thenReturn(mutableListOf(
            PixKey("c56dfef4-7901-44fb-84e2-a2cefb157890", KeyType.MOBILE_NUMBER, "+5511972651418", AccountType.CHECKING_ACCOUNT),
            PixKey("c56dfef4-7901-44fb-84e2-a2cefb157890", KeyType.MOBILE_NUMBER, "+5511972651418", AccountType.CHECKING_ACCOUNT)
        ))

        val exc = assertThrows<StatusRuntimeException> { blockingStub.deleteKey(request) }
        assertEquals(Status.INTERNAL.code, exc.status.code)
    }
}

@Factory
private class DeleteKeyClient {

    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceBlockingStub {
        return DeleteKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}