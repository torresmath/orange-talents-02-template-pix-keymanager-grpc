package br.com.torresmath.key.manager.shared

import io.micronaut.validation.validator.Validator
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class RequestValidator {

    @field:Inject
    lateinit var validator: Validator

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T> validate(target: T) where T : RequestDto {
        val constraintErrors = validator.validate(target)

        if (constraintErrors.isNotEmpty()) {
            throw ConstraintViolationException(constraintErrors.toMutableSet())
        }

        logger.info("Valid Request: $target")
    }
}