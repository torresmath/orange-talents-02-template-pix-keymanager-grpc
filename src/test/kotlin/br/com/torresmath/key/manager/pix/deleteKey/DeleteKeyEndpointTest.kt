package br.com.torresmath.key.manager.deleteKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.DeleteKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.DeleteKeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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

    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.update(defaultPixKey)
    }

    @AfterEach
    internal fun tearDown() = pixKeyRepository.deleteAll()

    companion object {
        val defaultPixKey = PixKey(
            "c56dfef4-7901-44fb-84e2-a2cefb157890",
            KeyType.CPF,
            "42549789873",
            AccountType.CHECKING_ACCOUNT
        )

        val clientId = defaultPixKey.clientId
        val pixId = defaultPixKey.pixUuid

        @JvmStatic
        fun notFoundParams() = listOf(
            Arguments.of(clientId, ""),
            Arguments.of("", pixId),
            Arguments.of("invalid", "id"),
            Arguments.of("", "")
        )
    }

    @ParameterizedTest
    @MethodSource("notFoundParams")
    fun `should return NOT_FOUND`(clientId: String, pixId: String) {

        val request = DeleteKeyRequest.newBuilder()
            .setClientId(clientId)
            .setPixId(pixId)
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.deleteKey(request) }
        assertEquals(Status.NOT_FOUND.code, exc.status.code)
    }

    @Test
    fun `should successfully mark to delete and return EMPTY`() {

        val request = DeleteKeyRequest.newBuilder()
            .setClientId(defaultPixKey.clientId)
            .setPixId(defaultPixKey.pixUuid)
            .build()

        val response = assertDoesNotThrow { blockingStub.deleteKey(request) }
        assertEquals(Empty.newBuilder().build(), response)
    }
}


/**
 * I wasn't sure how to reproduce a scenario with duplicated entries in db
 * (even manually is hard to introduce this bug in the project w/o changing PixKey model)
 * so I mocked a query retrieving duplicated data
 */
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
        ).thenReturn(
            mutableListOf(
                PixKey(
                    "c56dfef4-7901-44fb-84e2-a2cefb157890",
                    KeyType.MOBILE_NUMBER,
                    "+5511972651418",
                    AccountType.CHECKING_ACCOUNT
                ),
                PixKey(
                    "c56dfef4-7901-44fb-84e2-a2cefb157890",
                    KeyType.MOBILE_NUMBER,
                    "+5511972651418",
                    AccountType.CHECKING_ACCOUNT
                )
            )
        )

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