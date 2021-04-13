package br.com.torresmath.key.manager.pix.generateKey.commitKey

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("bcb")
interface BcbClient {

    @Post(value = "/api/v1/pix/keys", processes = [MediaType.APPLICATION_XML])
    fun generatePixKey(@Body request: BcbCreatePixKeyRequest): BcbCreatePixKeyResponse

}

data class BcbCreatePixKeyRequest(
    val keyType: String,
    val key: String,
    val bankAccount: BcbBankAccountRequest,
    val owner: BcbOwnerRequest
) {
    data class BcbBankAccountRequest(
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

    data class BcbOwnerRequest(
        val type: BcbOwnerType,
        val name: String,
        val taxIdNumber: String
    ) {
        enum class BcbOwnerType {
            NATURAL_PERSON,
            LEGAL_PERSON
        }
    }
}

data class BcbCreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BcbCreatePixKeyRequest.BcbBankAccountRequest,
    val owner: BcbCreatePixKeyRequest.BcbOwnerRequest,
    val createdAt: String
)
