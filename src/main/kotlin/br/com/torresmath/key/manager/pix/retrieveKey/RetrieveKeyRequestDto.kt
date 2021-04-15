package br.com.torresmath.key.manager.pix.retrieveKey

import br.com.torresmath.key.manager.RetrieveKeyRequest
import br.com.torresmath.key.manager.annotations.ValidUUID
import br.com.torresmath.key.manager.shared.RequestDto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class RetrieveKeyRequestDto(
    @NotNull
    @NotBlank
    @ValidUUID val clientId: String,
    @NotBlank
    @ValidUUID val pixId: String
) : RequestDto

fun RetrieveKeyRequest.toRequestDto(): RetrieveKeyRequestDto = RetrieveKeyRequestDto(this.clientId, this.pixId)