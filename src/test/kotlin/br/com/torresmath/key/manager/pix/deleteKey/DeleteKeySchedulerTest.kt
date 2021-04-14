package br.com.torresmath.key.manager.pix.deleteKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import br.com.torresmath.key.manager.pix.generateKey.PixKey
import br.com.torresmath.key.manager.pix.generateKey.PixKeyRepository
import br.com.torresmath.key.manager.pix.generateKey.PixKeyStatus
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbClient
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbDeletePixKeyRequest
import br.com.torresmath.key.manager.pix.generateKey.toPixKey
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import javax.inject.Inject

@MicronautTest
internal class DeleteKeySchedulerTest(
    @Inject
    val scheduler: DeleteKeyScheduler,
    @Inject
    val pixRepository: PixKeyRepository
) {

    @Inject
    lateinit var bcbMock: BcbClient

    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
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
        pixKey.status = PixKeyStatus.DELETE
        pixRepository.save(pixKey)
    }

    @Test
    fun `should delete`() {

        val req = BcbDeletePixKeyRequest(pixKey.keyIdentifier, "60701190")

        Mockito.`when`(bcbMock.deletePixKey(pixKey.keyIdentifier, req))
            .thenReturn(HttpResponse.ok())

        scheduler.deleteKeys()
        assertNull(pixRepository.findByKeyIdentifier(pixKey.keyIdentifier))
    }
}