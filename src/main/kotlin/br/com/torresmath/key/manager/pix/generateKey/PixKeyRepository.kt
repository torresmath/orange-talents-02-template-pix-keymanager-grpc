package br.com.torresmath.key.manager.pix.generateKey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {

    fun existsByKeyIdentifier(keyIdentifier: String): Boolean
    fun findByKeyIdentifier(keyIdentifier: String): PixKey?
    fun findByClientIdAndPixUuid(clientId: String, pixId: String): List<PixKey>

    fun findByStatus(status: PixKeyStatus, pageable: Pageable): Page<PixKey>
}
