package br.com.torresmath.key.manager.pix.listKey

import br.com.torresmath.key.manager.ListKeyGrpcServiceGrpc
import br.com.torresmath.key.manager.ListKeyRequest
import br.com.torresmath.key.manager.ListKeyResponse
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import br.com.torresmath.key.manager.shared.ErrorHandler
import br.com.torresmath.key.manager.shared.RequestValidator
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListKeyEndpoint(
    @Inject val repository: PixKeyRepository,
    @Inject val validator: RequestValidator
) : ListKeyGrpcServiceGrpc.ListKeyGrpcServiceImplBase() {

    override fun listKeyByClientId(request: ListKeyRequest?, responseObserver: StreamObserver<ListKeyResponse>?) {
        request!!
        validator.validate(request.toRequestDto())

        val keys: List<PixKey> = repository.findByClientId(request.clientId)

        val keysResponse = keys.map {

            val instant = it.createdDate.atZone(ZoneId.of("UTC")).toInstant()
            val createdAt = Timestamp.newBuilder()
                .setNanos(instant.nano)
                .setSeconds(instant.epochSecond)
                .build()

            ListKeyResponse.PixKey.newBuilder()
                .setStatus(it.status.toProtoKeyStatus())
                .setPixId(it.pixUuid)
                .setKey(it.keyIdentifier)
                .setKeyType(it.keyType)
                .setAccountType(it.accountType)
                .setCreatedAt(createdAt)
                .build()
        }

        val response = ListKeyResponse.newBuilder()
            .setClientId(request.clientId)
            .addAllKeys(keysResponse)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}
