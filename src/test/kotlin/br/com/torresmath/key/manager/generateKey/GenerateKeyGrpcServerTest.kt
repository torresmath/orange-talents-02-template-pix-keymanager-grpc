package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyResponse
import br.com.torresmath.key.manager.KeyType
import io.grpc.stub.StreamObserver
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import javax.inject.Inject
import javax.validation.ConstraintViolationException

@MicronautTest
internal class GenerateKeyGrpcServerTest {

    @Inject
    lateinit var server: GenerateKeyGrpcServer

    @Inject lateinit var validator: Validator

    @Test
    fun testConstraints() {
        val mock: StreamObserver<KeyResponse> = mock(StreamObserver::class.java) as StreamObserver<KeyResponse>
        val req = KeyRequest.newBuilder()
            .setAccountType(AccountType.CHECKING_ACCOUNT)
            .setClientId("")
            .setKeyIdentifier("")
            .setKeyType(KeyType.CPF)
            .build()
        server.generateKey(req, mock)
//        assertThrows(ConstraintViolationException::class.java) {
//
//        }
    }

}