package br.com.torresmath.key.manager.generateKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyRequest
import br.com.torresmath.key.manager.KeyType
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Validated
@Introspected
class KeyRequestModel(keyRequest: KeyRequest) {

    @field:NotBlank
    val clientId: String = keyRequest.clientId

    @field:NotNull
    val keyType: KeyType = keyRequest.keyType

    val keyIdentifier: String = keyRequest.keyIdentifier

    @field:NotNull
    val accountType: AccountType = keyRequest.accountType
}

fun KeyRequest.toModel() : KeyRequestModel { return KeyRequestModel(this) }