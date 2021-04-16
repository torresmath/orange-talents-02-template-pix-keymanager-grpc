package br.com.torresmath.key.manager.pix.listKey

import br.com.torresmath.key.manager.ListKeyRequest
import br.com.torresmath.key.manager.annotations.ValidUUID
import br.com.torresmath.key.manager.shared.RequestDto

class ListKeyRequestDto(@ValidUUID val clientId: String) : RequestDto

fun ListKeyRequest.toRequestDto(): ListKeyRequestDto = ListKeyRequestDto(this.clientId)