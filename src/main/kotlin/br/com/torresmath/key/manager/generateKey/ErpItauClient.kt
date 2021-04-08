package br.com.torresmath.key.manager.generateKey

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client(id = "erp-itau")
interface ErpItauClient {

    @Get("/api/v1/clientes/{identifier}")
    fun retrieveCustomer(@PathVariable identifier: String): ErpItauCustomer?
}

data class ErpItauCustomer(val id: String)
