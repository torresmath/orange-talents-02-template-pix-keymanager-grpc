package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyResponse
import br.com.torresmath.key.manager.KeyType
import io.grpc.stub.StreamObserver
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.verifyZeroInteractions
import javax.inject.Inject

@MicronautTest
internal class GenerateKeyGrpcServerTest {

    @Inject
    @InjectMocks
    lateinit var server: GenerateKeyGrpcServer

    @Inject
    lateinit var validator: Validator

    @Mock
    private var erpClient: ErpItauClient = mock(ErpItauClient::class.java)

    @Test
    fun testConstraints() {
        val mockObserver: StreamObserver<KeyResponse> = mock(StreamObserver::class.java) as StreamObserver<KeyResponse>
        val req = KeyRequest.newBuilder()
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .setClientId("")
            .setKeyIdentifier("")
            .setKeyType(KeyType.CPF)
            .build()

        server.generateKey(req, mockObserver)
        verifyZeroInteractions(erpClient)
    }

}