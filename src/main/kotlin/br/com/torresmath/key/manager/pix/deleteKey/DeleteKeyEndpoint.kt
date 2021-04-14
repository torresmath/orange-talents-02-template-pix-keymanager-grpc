package br.com.torresmath.key.manager.pix.deleteKey

import br.com.torresmath.key.manager.DeleteKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.DeleteKeyRequest
import br.com.torresmath.key.manager.pix.generateKey.PixKey
import br.com.torresmath.key.manager.pix.generateKey.PixKeyRepository
import com.google.protobuf.Empty
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteKeyEndpoint(
    @field:Inject val pixKeyRepository: PixKeyRepository
) : DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceImplBase() {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun deleteKey(request: DeleteKeyRequest?, responseObserver: StreamObserver<Empty>?) {

        request!!

        val pixKey: List<PixKey> = pixKeyRepository.findByClientIdAndPixUuid(request.clientId, request.pixId)

        when (pixKey.size) {
            0 -> {
                LOGGER.error("Not found pix key for client id ${request.clientId} and pix id ${request.pixId}")

                val statusProto = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Not found pix key for client id ${request.clientId} and pix id ${request.pixId}")
                    .build()

                responseObserver?.onError(io.grpc.protobuf.StatusProto.toStatusRuntimeException(statusProto))
            }
            1 -> {
                pixKey[0].markAsToDelete(pixKeyRepository)

                LOGGER.info("Successfully marked key to deletion for client id ${request.clientId} and pix id ${request.pixId}")
                responseObserver?.onNext(Empty.newBuilder().build())
                responseObserver?.onCompleted()
            }
            else -> {
                LOGGER.error("UNEXPECTED ERROR - It appears that there are ${pixKey.size} keys " +
                        "for client id ${request.clientId} and pix id ${request.pixId}")

                val statusProto = Status.newBuilder()
                    .setCode(Code.INTERNAL_VALUE)
                    .setMessage("Unexpected error")
                    .build()

                responseObserver?.onError(io.grpc.protobuf.StatusProto.toStatusRuntimeException(statusProto))
                throw IllegalStateException("UNEXPECTED ERROR - It appears that there are ${pixKey.size} keys " +
                        "for client id ${request.clientId} and pix id ${request.pixId}")
            }
        }

    }
}