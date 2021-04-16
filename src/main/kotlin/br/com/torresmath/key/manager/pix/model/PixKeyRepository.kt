package br.com.torresmath.key.manager.pix.model

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun existsByKeyIdentifier(keyIdentifier: String): Boolean
    fun findByKeyIdentifier(keyIdentifier: String): PixKey?
    fun findByClientIdAndPixUuid(clientId: String, pixId: String): List<PixKey>
    fun findByClientId(clientId: String): List<PixKey>
}
