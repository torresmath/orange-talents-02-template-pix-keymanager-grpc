package br.com.torresmath.key.manager.pix.generateKey.commitKey

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.pix.generateKey.ErpItauClient
import br.com.torresmath.key.manager.pix.model.PixRepositoryImpl
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CommitKeyScheduler(
    @field:Inject val erpItauClient: ErpItauClient,
    @field:Inject val bcbClient: BcbClient,
    @field:Inject val pixKeyRepositoryImpl: PixRepositoryImpl
) {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(fixedDelay = "10s")
    open fun commitKeys() {

        val inactiveKeys = pixKeyRepositoryImpl.findInactiveKeys()

        inactiveKeys.forEach { inactiveKey ->
            LOGGER.info("Try commit key: ${inactiveKey}")
            run {
                kotlin.runCatching {
                    erpItauClient.retrieveCustomerAccount(
                        inactiveKey.clientId,
                        inactiveKey.accountType.toErpItauValue()
                    )
                }.onSuccess {
                    it!!
                    LOGGER.info("Account retrieved: $it")
                    inactiveKey.commit(it, bcbClient, pixKeyRepositoryImpl)
                }.onFailure { throw it }
            }

        }
    }
}

fun AccountType.toErpItauValue(): String {
    return when (this) {
        AccountType.CHECKING_ACCOUNT -> "CONTA_CORRENTE"
        AccountType.SAVINGS_ACCOUNT -> "CONTA_POUPANCA"
        else -> ""
    }
}