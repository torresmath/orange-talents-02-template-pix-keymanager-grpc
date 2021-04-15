package br.com.torresmath.key.manager.pix.model

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.generateKey.ErpItauAccount
import br.com.torresmath.key.manager.pix.generateKey.ErpItauCustomer
import br.com.torresmath.key.manager.pix.generateKey.ErpItauInstitution
import br.com.torresmath.key.manager.pix.generateKey.commitKey.*
import br.com.torresmath.key.manager.pix.generateKey.toBcbBankAccountRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.exceptions.ReadTimeoutException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
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
    val repositoryImpl: PixRepositoryImpl,
    @field:Inject
    val pixRepository: PixKeyRepository
) {

    @field:Inject
    lateinit var bcbMock: BcbClient

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

    @BeforeEach
    internal fun setUp() {
        pixRepository.save(pixKey)
    }

    @AfterEach
    internal fun tearDown() {
        pixRepository.deleteAll()
    }

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

        pixKey.commit(itauAccount, bcbMock, repositoryImpl)
        assertEquals(PixKeyStatus.ACTIVE, pixKey.status)
    }

    @Test
    fun `should set key status as FAILED`() {
        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenThrow(HttpClientResponseException("UNPROCESSABLE_ENTITY", HttpResponse.unprocessableEntity<Any>()))

        pixKey.commit(itauAccount, bcbMock, repositoryImpl)
        assertEquals(PixKeyStatus.FAILED, pixKey.status)
    }

    @Test
    fun `should throw exception when service is down`() {
        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenThrow(ReadTimeoutException.TIMEOUT_EXCEPTION)

        assertThrows<ReadTimeoutException> { pixKey.commit(itauAccount, bcbMock, repositoryImpl) }
        assertEquals(PixKeyStatus.INACTIVE, pixKey.status)
    }

    @Test
    fun `should mark to delete`() {

        pixKey.markAsToDelete(pixRepository)
        assertEquals(PixKeyStatus.DELETE, pixKey.status)
    }

    @Test
    fun `should commit delete`() {

        val req = BcbDeletePixKeyRequest(pixKey.keyIdentifier, "60701190")

        Mockito.`when`(bcbMock.deletePixKey(pixKey.keyIdentifier, req))
            .thenReturn(HttpResponse.ok())

        pixKey.commitDeletion(req, bcbMock, repositoryImpl)
        assertNull(pixRepository.findByKeyIdentifier(pixKey.keyIdentifier))
    }

    @Test
    fun `should commit delete when client return 404`() {
        val req = BcbDeletePixKeyRequest(pixKey.keyIdentifier, "60701190")

        Mockito.`when`(bcbMock.deletePixKey(pixKey.keyIdentifier, req))
            .thenReturn(HttpResponse.notFound())

        assertDoesNotThrow { pixKey.commitDeletion(req, bcbMock, repositoryImpl) }
        assertNull(pixRepository.findByKeyIdentifier(pixKey.keyIdentifier))
    }

    @Test
    fun `should commit delete when client return 403`() {

        val req = BcbDeletePixKeyRequest(pixKey.keyIdentifier, "60701190")

        Mockito.`when`(bcbMock.deletePixKey(pixKey.keyIdentifier, req))
            .thenThrow(
                HttpClientResponseException(
                    "Forbidden",
                    HttpResponseFactory.INSTANCE.status<Any>(HttpStatus.FORBIDDEN)
                )
            )

        assertDoesNotThrow { pixKey.commitDeletion(req, bcbMock, repositoryImpl) }
        assertNull(pixRepository.findByKeyIdentifier(pixKey.keyIdentifier))
    }

    @Test
    fun `should not update when exception thrown`() {
        val req = BcbDeletePixKeyRequest(pixKey.keyIdentifier, "60701190")

        Mockito.`when`(bcbMock.deletePixKey(pixKey.keyIdentifier, req))
            .thenThrow(ReadTimeoutException.TIMEOUT_EXCEPTION)

        assertThrows<ReadTimeoutException> { pixKey.commitDeletion(req, bcbMock, repositoryImpl) }
        assertNotNull(pixRepository.findByKeyIdentifier(pixKey.keyIdentifier))
    }
}