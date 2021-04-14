package br.com.torresmath.key.manager.pix.generateKey.commitKey

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.retry.annotation.CircuitBreaker

@CircuitBreaker(delay = "5s", attempts = "3", excludes = [HttpClientResponseException::class])
@Client("bcb")
interface BcbClient {

    @Post(value = "/api/v1/pix/keys", processes = [MediaType.APPLICATION_XML])
    fun generatePixKey(@Body request: BcbCreatePixKeyRequest): BcbCreatePixKeyResponse

}

data class BcbCreatePixKeyRequest(
    val keyType: String,
    val key: String,
    val bankAccount: BcbBankAccount,
    val owner: BcbOwner
)

data class BcbBankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: BcbAccountType
) {
    enum class BcbAccountType {
        CACC,
        SVGS
    }
}

data class BcbOwner(
    val type: BcbOwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class BcbOwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

data class BcbCreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BcbBankAccount,
    val owner: BcbOwner,
    val createdAt: String
)
