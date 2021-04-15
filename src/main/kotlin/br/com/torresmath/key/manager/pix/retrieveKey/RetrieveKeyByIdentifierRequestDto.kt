package br.com.torresmath.key.manager.pix.retrieveKey

import br.com.torresmath.key.manager.RetrieveKeyByIdentifierRequest
import br.com.torresmath.key.manager.shared.RequestDto
import io.micronaut.validation.Validated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Validated
class RetrieveKeyByIdentifierRequestDto (@field:NotBlank @field:Size(max = 77) val key: String) : RequestDto

fun RetrieveKeyByIdentifierRequest.toRequestDto(): RetrieveKeyByIdentifierRequestDto {
    return RetrieveKeyByIdentifierRequestDto(this.key)
}