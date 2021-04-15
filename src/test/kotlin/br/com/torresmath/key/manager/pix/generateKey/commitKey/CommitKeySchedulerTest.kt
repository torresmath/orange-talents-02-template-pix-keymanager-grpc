package br.com.torresmath.key.manager.pix.generateKey.commitKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.generateKey.*
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import br.com.torresmath.key.manager.pix.model.PixRepositoryImpl
import io.micronaut.http.client.exceptions.ReadTimeoutException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import java.time.LocalDateTime
import javax.inject.Inject

@MicronautTest
internal class CommitKeySchedulerTest(
    @field:Inject val scheduler: CommitKeyScheduler,
    @field:Inject val repositoryImpl: PixRepositoryImpl,
    @field:Inject val testsRepository: PixKeyRepository
) {

    @field:Inject lateinit var  erpMock: ErpItauClient
    @field:Inject lateinit var bcbMock: BcbClient


    //region Mocks
    @MockBean(ErpItauClient::class)
    fun erpMock(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    //endregion

    val pixKey = PixKey("c56dfef4-7901-44fb-84e2-a2cefb157890", KeyType.CPF, "42549789873", AccountType.CHECKING_ACCOUNT)
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
        testsRepository.save(pixKey)
    }

    @AfterEach
    internal fun tearDown() {
        testsRepository.deleteAll()
    }

    @Test
    fun `should commit key`() {

        Mockito.`when`(erpMock.retrieveCustomerAccount(pixKey.clientId, "CONTA_CORRENTE"))
            .thenReturn(itauAccount)

        Mockito.`when`(bcbMock.generatePixKey(bcbRequest))
            .thenReturn(BcbCreatePixKeyResponse(
                bcbRequest.keyType,
                bcbRequest.key,
                bcbRequest.bankAccount,
                bcbRequest.owner,
                LocalDateTime.now().toString()
            ))

        assertDoesNotThrow { scheduler.commitKeys() }
        verify(bcbMock, times(1)).generatePixKey(bcbRequest)
    }

    @Test
    fun `should throw exception when service is down`() {

        Mockito.`when`(erpMock.retrieveCustomerAccount(pixKey.clientId, "CONTA_CORRENTE"))
            .thenThrow(ReadTimeoutException.TIMEOUT_EXCEPTION)

        org.junit.jupiter.api.assertThrows<ReadTimeoutException> { scheduler.commitKeys() }
        verifyZeroInteractions(bcbMock)
    }
}