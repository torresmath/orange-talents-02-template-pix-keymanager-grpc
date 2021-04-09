package br.com.torresmath.key.manager

import br.com.torresmath.key.manager.generateKey.toPixKey
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import io.micronaut.validation.validator.Validator
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestConstraintValidator {

    @Inject
    lateinit var validator: Validator

    private val logger = LoggerFactory.getLogger(RequestConstraintValidator::class.java)

    fun validate(request: KeyRequest): Status? {
        val requestModel = request.toPixKey()

        val validation = validator.validate(requestModel)

        val validIdentifier = requestModel.isValidIdentifier(validator)

        if (validation.isEmpty() && validIdentifier) {
            logger.info("Valid Request: $request")
            return null
        }

        val errors = validation.map {
            logger.info("Request Error:  ${it.message}")
            ErrorDetail.newBuilder()
                .setMessage(it.message)
                .setCode(404)
                .build()
        }

        val errorDetails = ErrorDetails.newBuilder()
            .addAllDetails(errors)

        if (validIdentifier.not()) {
            errorDetails.addDetails(
                ErrorDetail.newBuilder()
                    .setMessage("Invalid key identifier: ${requestModel.keyIdentifier} - Given Key Type: ${requestModel.keyType}")
                    .setCode(404)
                    .build()
            )
        }

        return Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT.number)
            .setMessage(errorDetails.getDetails(0).message)
            .addDetails(
                Any.pack(errorDetails.build())
            ).build()
    }
}