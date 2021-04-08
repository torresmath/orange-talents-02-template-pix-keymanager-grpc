package br.com.torresmath.key.manager

import br.com.torresmath.key.manager.generateKey.toModel
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
        val validation = validator.validate(request.toModel())

        if (validation.isEmpty()) {
            logger.info("Valid Request: $request")
            return null
        }

        val errors = validation.map {
            logger.info("E: ${it.message}")
            ErrorDetail.newBuilder()
                .setMessage(it.message)
                .setCode(404)
                .build()
        }

        val errorDetails = ErrorDetails.newBuilder()
            .addAllDetails(errors)
            .build()

        return Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT.number)
            .setMessage("Bad request")
            .addDetails(
                Any.pack(
                    errorDetails
                )
            ).build()
    }
}