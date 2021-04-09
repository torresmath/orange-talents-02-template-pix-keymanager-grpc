package br.com.torresmath.key.manager.exceptions

class PixKeyAlreadyExistsException(
    message: String,
    val code: Int = 422
) : RuntimeException(message)