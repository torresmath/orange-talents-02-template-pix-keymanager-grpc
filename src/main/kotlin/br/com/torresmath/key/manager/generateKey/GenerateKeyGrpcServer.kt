package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateKeyGrpcServer(
    @Inject val itauClient: ErpItauClient,
    @Inject val requestValidator: RequestConstraintValidator
) :
    GenerateKeyGrpcServiceGrpc.GenerateKeyGrpcServiceImplBase() {

    override fun generateKey(request: KeyRequest?, responseObserver: StreamObserver<KeyResponse>?) {

        println("GENERATE KEY")
        request!!

        val error = requestValidator.validate(request)

        if (error != null)
            responseObserver?.onError(io.grpc.protobuf.StatusProto.toStatusRuntimeException(error))

        val retrieveCustomer = itauClient.runCatching { retrieveCustomer(request.clientId) }

        if (retrieveCustomer.isFailure) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Couldn't find customer with given identifier: ${request.clientId}")
                    .withCause(retrieveCustomer.exceptionOrNull())
                    .asRuntimeException()
            )
        }

    }
}
