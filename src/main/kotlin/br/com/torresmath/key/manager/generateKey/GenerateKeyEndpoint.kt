package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.GenerateKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyResponse
import br.com.torresmath.key.manager.RequestConstraintValidator
import br.com.torresmath.key.manager.exceptions.NotFoundCustomerException
import br.com.torresmath.key.manager.exceptions.PixKeyAlreadyExistsException
import br.com.torresmath.key.manager.shared.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class GenerateKeyEndpoint(
    @Inject val itauClient: ErpItauClient,
    @Inject val requestValidator: RequestConstraintValidator,
    @Inject val pixKeyRepository: PixKeyRepository
) : GenerateKeyGrpcServiceGrpc.GenerateKeyGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun generateKey(request: KeyRequest?, responseObserver: StreamObserver<KeyResponse>?) {

        request!!

        val pixKey = request.toPixKey()

        requestValidator.validate(pixKey)

        itauClient.runCatching { retrieveCustomer(request.clientId) }
            .onFailure {
                throw NotFoundCustomerException("Couldn't find customer with given identifier: ${request.clientId}")
            }

        val keyIsNotUnique = pixKeyRepository.existsByKeyIdentifier(pixKey.keyIdentifier)

        if (keyIsNotUnique) {
            throw PixKeyAlreadyExistsException(
                "There's already a pix key registered with provided key identifier: ${request.keyIdentifier}"
            )
        }

        pixKeyRepository.save(pixKey)

        logger.info("Successfully created Pix Key: ${pixKey.pixUuid}")
        val response = KeyResponse.newBuilder()
            .setPixId(pixKey.pixUuid)
            .build()

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}
