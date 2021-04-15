package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.pix.model.PixKey
import io.micronaut.validation.validator.Validator
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class RequestConstraintValidator {

    @Inject
    lateinit var validator: Validator

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun validate(pixKey: PixKey) {

        val constraintErrors = validator.validate(pixKey)

        if (constraintErrors.isNotEmpty()) {
            throw ConstraintViolationException(constraintErrors.toMutableSet())
        }

        logger.info("Valid Pix Key Request: $pixKey")
        return
    }
}