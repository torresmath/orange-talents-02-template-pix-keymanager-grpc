package br.com.torresmath.key.manager.pix.model

import br.com.torresmath.key.manager.AccountType
import br.com.torresmath.key.manager.KeyType
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.TypedQuery

@MicronautTest
internal class PixRepositoryImplTest(
    @Inject val repository: PixRepositoryImpl
) {

    @field:Inject
    lateinit var mockManager: EntityManager

    @MockBean(EntityManager::class)
    fun mockManager() : EntityManager {
        return Mockito.mock(EntityManager::class.java)
    }

    @Test
    fun `should throw INTERNAL`() {

        val uuid = UUID.randomUUID().toString()

        val typedQuery: TypedQuery<PixKey> = Mockito.mock(TypedQuery::class.java) as TypedQuery<PixKey>


        Mockito.`when`(mockManager.createQuery("select p from PixKey p where p.clientId = :clientId and p.pixUuid = :pixId", PixKey::class.java))
            .thenReturn(typedQuery)

        Mockito.`when`(typedQuery.setParameter(Mockito.anyString(), Mockito.any()))
            .thenReturn(typedQuery)

        Mockito.`when`(typedQuery.resultList)
            .thenReturn(mutableListOf(
                PixKey(uuid, KeyType.CPF, "42549789873", AccountType.CHECKING_ACCOUNT),
                PixKey(uuid, KeyType.CPF, "42549789873", AccountType.CHECKING_ACCOUNT)
            ))

        assertThrows<IllegalStateException> { repository.findByClientIdAndPixUuid(uuid, uuid) }
    }
}