package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.GenerateKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.generateKey.commitKey.toErpItauValue
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import br.com.torresmath.key.manager.pix.model.toPixKey
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.inject.Inject

@MicronautTest
internal class GenerateKeyEndpointTest(
    @Inject
    val blockingStub: GenerateKeyGrpcServiceGrpc.GenerateKeyGrpcServiceBlockingStub,
    @Inject
    val requestValidator: RequestConstraintValidator,
) {

    @field:Inject
    lateinit var keyRepository: PixKeyRepository

    @Inject
    lateinit var erpMock: ErpItauClient

    @MockBean(ErpItauClient::class)
    fun erpMock(): ErpItauClient {
        return mock(ErpItauClient::class.java)
    }

    var defaultRequest: KeyRequest = KeyRequest.newBuilder()
        .setKeyType(KeyType.CPF)
        .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
        .setKeyIdentifier("42549789873")
        .setAccountType(AccountType.CHECKING_ACCOUNT)
        .build()

    var pixKey: PixKey = defaultRequest.toPixKey()

    @BeforeEach
    internal fun setUp() {
        keyRepository.save(pixKey)
    }

    @AfterEach
    internal fun tearDown() {
        keyRepository.deleteAll()
    }

    @Test
    fun `should return NOT_FOUND`() {
        val request = KeyRequest.newBuilder()
            .setKeyType(KeyType.RANDOM)
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .build()

        Mockito.`when`(
            erpMock.retrieveCustomerAccount(request.clientId, request.accountType.toErpItauValue())
        ).thenThrow(HttpClientResponseException("Not found customer", HttpResponse.notFound("")))

        val exc = assertThrows<StatusRuntimeException> { blockingStub.generateKey(request) }
        assertEquals(Status.NOT_FOUND.code, exc.status.code)
    }

    @Test
    fun `should return ALREADY_EXISTS`() {
        Mockito.`when`(
            erpMock.retrieveCustomerAccount(defaultRequest.clientId, defaultRequest.accountType.toErpItauValue())
        ).thenReturn(
            ErpItauAccount(
                "CONTA_CORRENTE",
                ErpItauInstitution("ITAÚ UNIBANCO S.A.", "60701190"),
                "0001",
                "291900",
                ErpItauCustomer(defaultRequest.clientId, "User test", defaultRequest.keyIdentifier)
            )
        )

        val exc = assertThrows<StatusRuntimeException> { blockingStub.generateKey(defaultRequest) }
        assertEquals(Status.ALREADY_EXISTS.code, exc.status.code)
    }

    companion object {
        @JvmStatic
        fun validParams() = listOf(
            Arguments.of(KeyType.CPF, "78416416052"),
            Arguments.of(KeyType.EMAIL, "email@test.com"),
            Arguments.of(KeyType.MOBILE_NUMBER, "+55972651418")
        )

        @JvmStatic
        fun invalidParams() = listOf(
            Arguments.of(KeyType.CPF, ""),
            Arguments.of(KeyType.CPF, "111111111"),
            Arguments.of(KeyType.EMAIL, "email"),
            Arguments.of(KeyType.EMAIL, ""),
            Arguments.of(KeyType.MOBILE_NUMBER, "55972651418"),
            Arguments.of(KeyType.MOBILE_NUMBER, "11111111111"),
            Arguments.of(KeyType.MOBILE_NUMBER, "+0011111111")
        )
    }

    @ParameterizedTest
    @MethodSource("validParams")
    fun `should save successfully`(keyType: KeyType, identifier: String) {
        val request = KeyRequest.newBuilder()
            .setKeyType(keyType)
            .setClientId("232ddbc6-9b9d-11eb-a8b3-0242ac130003")
            .setKeyIdentifier(identifier)
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .build()

        Mockito.`when`(
            erpMock.retrieveCustomerAccount("232ddbc6-9b9d-11eb-a8b3-0242ac130003", request.accountType.toErpItauValue())
        ).thenReturn(
            ErpItauAccount(
                "CONTA_CORRENTE",
                ErpItauInstitution("ITAÚ UNIBANCO S.A.", "60701190"),
                "0001",
                "291900",
                ErpItauCustomer(defaultRequest.clientId, "User test", defaultRequest.keyIdentifier)
            )
        )

        val response = blockingStub.generateKey(request)

        val savedKey: PixKey? = keyRepository.findByKeyIdentifier(request.keyIdentifier)
        assertTrue(savedKey != null)
        assertEquals(savedKey?.pixUuid, response.pixId)
    }

    @ParameterizedTest
    @MethodSource("invalidParams")
    fun `should return error invalid key identifier`(keyType: KeyType, identifier: String) {

        val request = KeyRequest.newBuilder()
            .setKeyType(keyType)
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setKeyIdentifier(identifier)
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .build()

        val exc = assertThrows<StatusRuntimeException> { blockingStub.generateKey(request) }
        with(exc) {
            assertEquals(Status.INVALID_ARGUMENT.code, exc.status.code)
            assertEquals("Request with invalid parameters!", exc.status.description)

            assertThat(
                violations(), containsInAnyOrder(
                    Pair("PixKey", "Invalid pix key identifier for provided pix key type")
                )
            )
        }
    }

    private fun StatusRuntimeException.violations(): List<Pair<String, String>>? {
        val details = StatusProto.fromThrowable(this)
            ?.detailsList?.get(0)!!
            .unpack(BadRequest::class.java)

        return details.fieldViolationsList
            .map { it.field to it.description }
    }
}

@Factory
private class GenerateKeyClient {

    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            GenerateKeyGrpcServiceGrpc.GenerateKeyGrpcServiceBlockingStub {
        return GenerateKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}