package br.com.torresmath.key.manager.pix.generateKey

import br.com.torresmath.key.manager.KeyStatus

enum class PixKeyStatus {
    INACTIVE,
    ACTIVE;

    fun toProtoKeyStatus(): KeyStatus {
        return when (this) {
            INACTIVE -> KeyStatus.INACTIVE
            ACTIVE -> KeyStatus.ACTIVE
        }
    }
}