package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbBankAccount
import br.com.torresmath.key.manager.pix.model.Account
import br.com.torresmath.key.manager.pix.model.AccountOwner
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.retry.annotation.CircuitBreaker

@CircuitBreaker(delay = "5s", attempts = "3", excludes = [HttpClientResponseException::class])
@Client(id = "erp-itau")
interface ErpItauClient {

    @Get("/api/v1/clientes/{identifier}/contas")
    fun retrieveCustomerAccount(@PathVariable identifier: String, @QueryValue("tipo") type: String): ErpItauAccount
}

data class ErpItauAccount(
    val tipo: String,
    val instituicao: ErpItauInstitution,
    val agencia: String,
    val numero: String,
    val titular: ErpItauCustomer
) {
    fun toAccount(): Account {
        return Account(
            branch = agencia,
            number = numero,
            owner = AccountOwner(
                name = titular.nome,
                cpf = titular.cpf
            )
        )
    }
}

data class ErpItauCustomer(
    val id: String,
    val nome: String,
    val cpf: String
)

data class ErpItauInstitution(
    val nome: String,
    val ispb: String
)

fun ErpItauAccount.toBcbBankAccountRequest(): BcbBankAccount {
    return BcbBankAccount(
        this.instituicao.ispb,
        this.agencia,
        this.numero,
        stringToBcbAccountType(this.tipo)
    )
}

fun stringToBcbAccountType(value: String): BcbBankAccount.BcbAccountType {
    return when (value) {
        "CONTA_CORRENTE" -> BcbBankAccount.BcbAccountType.CACC
        "CONTA_POUPANCA" -> BcbBankAccount.BcbAccountType.SVGS
        else -> throw IllegalStateException("ERROR - Impossible conversion: $value to BcbAccountType")
    }
}