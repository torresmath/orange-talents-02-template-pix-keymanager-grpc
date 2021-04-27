package br.com.torresmath.key.manager.pix.model

import br.com.torresmath.key.manager.exceptions.NotFoundPixKeyException
import io.micronaut.data.annotation.Repository
import io.micronaut.transaction.SynchronousTransactionManager
import org.hibernate.LockOptions
import java.sql.Connection
import javax.persistence.EntityManager
import javax.persistence.LockModeType

@Repository
open class PixRepositoryImpl(
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

    fun findKeysMarkedToDelete(): List<PixKey> {
        return transactional.executeRead {
            em.createQuery("select p from PixKey p where p.status = :status", PixKey::class.java)
                .setMaxResults(5)
                .setParameter("status", PixKeyStatus.DELETE)
                .setHint(
                    "javax.persistence.lock.timeout",
                    LockOptions.SKIP_LOCKED)
                .resultList
        }
    }

    fun update(pixKey: PixKey): PixKey? {
        return transactional.executeWrite {
            em.merge(pixKey)
        }
    }

    fun delete(pixKey: PixKey) {
        return transactional.executeWrite {
            em.remove(pixKey)
        }
    }

    fun findByClientIdAndPixUuid(clientId: String, pixId: String): PixKey {
        val pixKeys = transactional.executeRead {
            em.createQuery("select p from PixKey p where p.clientId = :clientId and p.pixUuid = :pixId", PixKey::class.java)
                .setParameter("clientId", clientId)
                .setParameter("pixId", pixId)
                .resultList
        }

        when (pixKeys.size) {
            0 -> throw NotFoundPixKeyException("Not found pix key for client id $clientId and pix id $pixId")
            1 -> return pixKeys[0]
            else -> throw IllegalStateException("UNEXPECTED ERROR - It appears that there are ${pixKeys.size} keys " +
                    "for client id $clientId and pix id $pixId")
        }
    }

}