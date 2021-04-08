package br.com.torresmath.key.manager.annotations

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidEnumValidator::class])
annotation class ValidEnum(
    val message: String = "Invalid value. Please check valid inputs in documentation",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
    val targetEnum: KClass<out Enum<*>>
)

@Singleton
class ValidEnumValidator : ConstraintValidator<ValidEnum, String> {

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidEnum>,
        context: io.micronaut.validation.validator.constraints.ConstraintValidatorContext
    ): Boolean {

        println("VALIDATING")
        val targetEnum: Class<out Enum<*>> = annotationMetadata.classValue("targetEnum").get() as Class<out Enum<*>>

        val contains = targetEnum.enumConstants.map { e -> e.name }
            .contains(value)
        println("Contains? $contains")
        return contains

    }

}
