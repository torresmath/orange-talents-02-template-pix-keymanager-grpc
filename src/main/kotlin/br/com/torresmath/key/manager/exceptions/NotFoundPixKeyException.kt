package br.com.torresmath.key.manager.exceptions

class NotFoundPixKeyException(message: String?, val code: Int = 404) : RuntimeException(message)