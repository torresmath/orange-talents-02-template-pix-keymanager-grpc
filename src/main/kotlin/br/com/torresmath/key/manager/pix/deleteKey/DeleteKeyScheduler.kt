package br.com.torresmath.key.manager.pix.deleteKey

import br.com.torresmath.key.manager.pix.PixRepositoryImpl
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbClient
import br.com.torresmath.key.manager.pix.generateKey.commitKey.BcbDeletePixKeyRequest
import io.micronaut.context.annotation.Value
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteKeyScheduler(
    @field:Inject val pixRepositoryImpl: PixRepositoryImpl,
    @field:Inject val bcbClient: BcbClient
) {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    @Value("\${api.participant}")
    lateinit var participant: String

    @Scheduled(fixedDelay = "10s")
    fun deleteKeys() {

        val markedKeys = pixRepositoryImpl.findKeysMarkedToDelete()

        markedKeys.forEach {
            LOGGER.info("Try delete key; $it")
            val bcbRequest = BcbDeletePixKeyRequest(it.keyIdentifier, participant)
            it.commitDeletion(bcbRequest, bcbClient, pixRepositoryImpl)
        }

    }
}