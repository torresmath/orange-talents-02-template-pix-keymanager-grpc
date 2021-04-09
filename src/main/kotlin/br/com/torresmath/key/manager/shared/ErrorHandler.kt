package br.com.torresmath.key.manager.shared

import io.micronaut.aop.Around

@Around // Necessário para permitir que um Interceptor atue sobre o método
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ErrorHandler()
