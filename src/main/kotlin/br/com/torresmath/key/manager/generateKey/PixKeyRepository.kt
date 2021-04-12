package br.com.torresmath.key.manager.generateKey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {
    fun existsByKeyIdentifier(keyIdentifier: String) : Boolean
    fun findByKeyIdentifier(keyIdentifier: String): PixKey?
}
