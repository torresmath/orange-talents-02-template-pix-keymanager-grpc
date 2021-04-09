package br.com.torresmath.key.manager.shared

import br.com.torresmath.key.manager.ErrorDetail
import br.com.torresmath.key.manager.exceptions.NotFoundCustomerException
import br.com.torresmath.key.manager.exceptions.PixKeyAlreadyExistsException
import br.com.torresmath.key.manager.generateKey.GenerateKeyEndpoint
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor : MethodInterceptor<GenerateKeyEndpoint, Any?>{

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(context: MethodInvocationContext<GenerateKeyEndpoint, Any?>): Any? {
        try {
            return context.proceed()
        } catch (e: Exception) {
            val statusError = when (e) {
//                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
//                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> handleConstraintValidationException(e)
                is PixKeyAlreadyExistsException -> handlePixAlreadyExistsException(e)
                is NotFoundCustomerException -> handleNotFoundCustomerException(e)
                else -> Status.UNKNOWN.withDescription("unexpected error happened").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }
    }

    private fun handleNotFoundCustomerException(e: NotFoundCustomerException): StatusRuntimeException {
        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.NOT_FOUND_VALUE)
            .setMessage(e.message)
            .addDetails(
                com.google.protobuf.Any.pack(
                    ErrorDetail.newBuilder()
                        .setCode(e.code)
                        .setMessage(e.message)
                        .build()
                )
            ).build()

        return StatusProto.toStatusRuntimeException(statusProto)
    }

    private fun handlePixAlreadyExistsException(e: PixKeyAlreadyExistsException): StatusRuntimeException {

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.ALREADY_EXISTS_VALUE)
            .setMessage(e.message)
            .addDetails(
                com.google.protobuf.Any.pack(
                    ErrorDetail.newBuilder()
                        .setCode(e.code)
                        .setMessage(e.message)
                        .build()
                )
            ).build()

        return StatusProto.toStatusRuntimeException(statusProto)
    }

    private fun handleConstraintValidationException(e: ConstraintViolationException) : StatusRuntimeException {

        val badRequest = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name?:it.rootBeanClass.simpleName) // In case of class level Constraints
                    .setDescription(it.message)
                    .build()
            }).build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Request with invalid parameters!")
            .addDetails(com.google.protobuf.Any.pack(badRequest))
            .build()

        LOGGER.info("$statusProto")
        return StatusProto.toStatusRuntimeException(statusProto)
    }
}