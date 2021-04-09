package br.com.torresmath.key.manager.exceptions

class NotFoundCustomerException(message: String?, val code: Int = 404) : RuntimeException(message) {
}