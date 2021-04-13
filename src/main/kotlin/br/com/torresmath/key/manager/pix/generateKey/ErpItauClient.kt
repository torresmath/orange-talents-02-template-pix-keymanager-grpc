package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbCreatePixKeyRequest
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(id = "erp-itau")
interface ErpItauClient {

    @Get("/api/v1/clientes/{identifier}")
    fun retrieveCustomer(@PathVariable identifier: String): ErpItauCustomer?

    @Get("/api/v1/clientes/{identifier}/contas")
    fun retrieveCustomerAccount(@PathVariable identifier: String, @QueryValue("tipo") type: String) : ErpItauAccount?
}

data class ErpItauCustomer(
    val id: String,
    val nome: String,
    val cpf: String
)

data class ErpItauAccount(
    val tipo: String,
    val instituicao: ErpItauInstitution,
    val agencia: String,
    val numero: String,
    val titular: ErpItauCustomer
)

data class ErpItauInstitution(
    val nome: String,
    val ispb: String
)

fun ErpItauAccount.toBcbBankAccountRequest(): BcbCreatePixKeyRequest.BcbBankAccountRequest {
    return BcbCreatePixKeyRequest.BcbBankAccountRequest(
        this.instituicao.ispb,
        this.agencia,
        this.numero,
        stringToBcbAccountType(this.tipo)
    )
}

fun stringToBcbAccountType(value: String): BcbCreatePixKeyRequest.BcbBankAccountRequest.BcbAccountType {
    return when (value) {
        "CONTA_CORRENTE" -> BcbCreatePixKeyRequest.BcbBankAccountRequest.BcbAccountType.CACC
        "CONTA_POUPANCA" -> BcbCreatePixKeyRequest.BcbBankAccountRequest.BcbAccountType.SVGS
        else -> throw IllegalStateException("ERROR - Impossible conversion: $value to BcbAccountType")
    }
}