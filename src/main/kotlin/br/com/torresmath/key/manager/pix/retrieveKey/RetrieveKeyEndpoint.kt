package br.com.torresmath.key.manager.pix.retrieveKey

import br.com.torresmath.key.manager.*
import br.com.torresmath.key.manager.pix.model.PixKey
import br.com.torresmath.key.manager.pix.model.PixKeyRepository
import br.com.torresmath.key.manager.shared.ErrorHandler
import br.com.torresmath.key.manager.shared.RequestValidator
import com.google.protobuf.Timestamp
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RetrieveKeyEndpoint(
    @Inject val validator: RequestValidator,
    @Inject val repository: PixKeyRepository
) : RetrieveKeyGrpcServiceGrpc.RetrieveKeyGrpcServiceImplBase() {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun retrieveKey(request: RetrieveKeyRequest?, responseObserver: StreamObserver<KeyDetailResponse>?) {

        request!!
        validator.validate(request.toRequestDto())

        val pixKey = repository.findByClientIdAndPixUuid(request.clientId, request.pixId)

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
                val key = pixKey[0]
                LOGGER.info("Successfully marked key to deletion for client id ${request.clientId} and pix id ${request.pixId}")
                responseObserver?.onNext(buildResponse(key))
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

private fun buildResponse(pixKey: PixKey) : KeyDetailResponse {

    val account = pixKey.account!!

    val accountResponse = KeyAccountResponse.newBuilder()
        .setNumber(account.number)
        .setBranch(account.branch)
        .setInstitution(
            KeyAccountInstitutionResponse.newBuilder()
                .setName("ITAÚ UNIBANCO S.A.")
                .setIsbn("60701190")
                .build()
        )
        .setType(pixKey.accountType)
        .build()

    val ownerResponse = KeyOwnerResponse.newBuilder()
        .setCpf(account.owner.cpf)
        .setName(account.owner.name)
        .build()

    val instant = pixKey.createdDate.atZone(ZoneId.of("UTC")).toInstant()

    val createdAt = Timestamp.newBuilder()
        .setNanos(instant.nano)
        .setSeconds(instant.epochSecond)
        .build()

    return KeyDetailResponse.newBuilder()
        .setClientId(pixKey.clientId)
        .setPixId(pixKey.pixUuid)
        .setCreatedAt(createdAt)
        .setAccount(accountResponse)
        .setOwner(ownerResponse)
        .build()
}