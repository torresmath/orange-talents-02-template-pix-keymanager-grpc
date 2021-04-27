package br.com.torresmath.key.manager.pix.deleteKey

import br.com.torresmath.key.manager.DeleteKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.DeleteKeyRequest
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import br.com.torresmath.key.manager.pix.model.PixRepositoryImpl
import br.com.torresmath.key.manager.shared.ErrorHandler
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class DeleteKeyEndpoint(
    @field:Inject val pixKeyRepositoryImpl: PixRepositoryImpl
) : DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceImplBase() {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun deleteKey(request: DeleteKeyRequest?, responseObserver: StreamObserver<Empty>?) {

        request!!

        val pixKey: PixKey = pixKeyRepositoryImpl.findByClientIdAndPixUuid(request.clientId, request.pixId)
        pixKey.markAsToDelete(pixKeyRepositoryImpl)

        LOGGER.info("Successfully marked key to deletion for client id ${request.clientId} and pix id ${request.pixId}")
        responseObserver?.onNext(Empty.newBuilder().build())
        responseObserver?.onCompleted()
    }
}