package br.com.torresmath.key.manager.pix.generateKey.commitKey

import br.com.torresmath.key.manager.pix.generateKey.PixKey
import br.com.torresmath.key.manager.pix.generateKey.PixKeyStatus
import io.micronaut.data.annotation.Repository
import io.micronaut.transaction.SynchronousTransactionManager
import org.hibernate.LockOptions
import java.sql.Connection
import javax.persistence.EntityManager
import javax.persistence.LockModeType

@Repository
open class InactivePixRepository(
    val em: EntityManager,
    private val transactional: SynchronousTransactionManager<Connection>
) {

    fun findInactiveKeys(): List<PixKey> {
        return transactional.executeRead {
            em.createQuery("select p from PixKey p where p.status = :status", PixKey::class.java)
                .setMaxResults(5)
                .setParameter("status", PixKeyStatus.INACTIVE)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setHint(
                    "javax.persistence.lock.timeout",
                    LockOptions.SKIP_LOCKED
                )
                .resultList
        }
    }

    fun update(pixKey: PixKey): PixKey? {
        return transactional.executeWrite {
            em.merge(pixKey)
        }
    }

}