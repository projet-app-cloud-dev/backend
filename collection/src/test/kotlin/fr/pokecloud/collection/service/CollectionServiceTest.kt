package fr.pokecloud.collection.service

import fr.pokecloud.collection.database.entities.CardCollection
import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.database.repository.CollectionCardRepository
import fr.pokecloud.collection.database.repository.CollectionRepository
import fr.pokecloud.collection.model.exceptions.CollectionNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import fr.pokecloud.collection.database.entities.Card as CardEntity
import fr.pokecloud.collection.database.entities.Collection as CollectionEntity


@SpringBootTest
@AutoConfigureMockMvc
class CollectionServiceTest {
    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    lateinit var collectionService: CollectionService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var cardCollectionRepository: CollectionCardRepository

    @Autowired
    lateinit var cardRepository: CardRepository

    @BeforeEach
    fun setUp() {
        collectionRepository.deleteAll()
        cardRepository.deleteAll()
        cardCollectionRepository.deleteAll()
    }

    @Test
    fun `test get existing collection without cards return said collection`() {
        val ent = collectionRepository.save(
            CollectionEntity(
                null, 1L, "foo", listOf()
            )
        )

        val col = collectionService.getCollection(ent.id!!)
        assertNotNull(col)

        assertEquals(ent.id, col.id)
        assertEquals(ent.ownerId, col.userId)
        assertEquals(ent.name, col.name)
        assertEquals(0, col.cards.size)
    }

    fun insertCollectionWithCards(): CollectionEntity {
        val def = DefaultTransactionDefinition()
        def.setName("SomeTxName")
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED

        val status = transactionManager.getTransaction(def)
        try {
            // put your business logic here
            val cards = cardRepository.saveAll(List(25) {
                CardEntity(it.toLong(), "card-$it", listOf())
            })


            val ent = collectionRepository.save(
                CollectionEntity(
                    null, 1L, "foo", listOf()
                )
            )

            val cardCollections = cardCollectionRepository.saveAll(cards.mapIndexed { index, card ->
                CardCollection(card, ent, index.toLong() + 1)
            })
            transactionManager.commit(status)
            return ent

        } catch (ex: Exception) {
            transactionManager.rollback(status)
            throw ex
        }
    }

    @Test
    fun `test get existing collection with cards return said collection`() {
        val ent = insertCollectionWithCards()
        val col = collectionService.getCollection(ent.id!!)
        assertNotNull(col)

        assertEquals(ent.id, col.id)
        assertEquals(ent.ownerId, col.userId)
        assertEquals(ent.name, col.name)
        assertEquals(25, col.cards.size)

        col.cards.forEach {
            assertEquals("card-${it.id}", it.name)
            assertEquals(1 + it.id, it.count)
        }
    }


    @Test
    fun `test get not existing collection should return null`() {
        val col = collectionService.getCollection(3L)
        assertNull(col)
    }

    @Test
    fun `test get collections without user id return correct elements`() {
        collectionRepository.saveAll(List(15) {
            CollectionEntity(null, 1L, "collection-$it", arrayListOf())
        })
        collectionRepository.saveAll(List(15) {
            CollectionEntity(null, 1L, "collection-${15 + it}", arrayListOf())
        })
        var collections = collectionService.getCollections(0, null)
        assertEquals(25, collections.count)
        assertEquals(30, collections.totalCount)
        for (i in collections.collections.indices) {
            assertEquals(collections.collections[i].name, "collection-$i")
        }
        collections = collectionService.getCollections(1, null)
        assertEquals(5, collections.count)
        assertEquals(30, collections.totalCount)
        for (i in collections.collections.indices) {
            assertEquals(collections.collections[i].name, "collection-${25 + i}")
        }
        collections = collectionService.getCollections(2, null)
        assertEquals(0, collections.count)
        assertEquals(30, collections.totalCount)
    }

    @Test
    fun `test get collections with user id set to owner return correct elements`() {
        collectionRepository.saveAll(List(30) {
            CollectionEntity(null, 1L, "collection-$it", arrayListOf())
        })
        collectionRepository.saveAll(List(30) {
            CollectionEntity(null, 2L, "collection-$it", arrayListOf())
        })
        var collections = collectionService.getCollections(0, 1L)
        assertEquals(25, collections.count)
        assertEquals(30, collections.totalCount)
        for (i in collections.collections.indices) {
            assertEquals(collections.collections[i].name, "collection-$i")
        }
        collections = collectionService.getCollections(1, 1L)
        assertEquals(5, collections.count)
        assertEquals(30, collections.totalCount)
        for (i in collections.collections.indices) {
            assertEquals(collections.collections[i].name, "collection-${25 + i}")
        }
        collections = collectionService.getCollections(2, 1L)
        assertEquals(0, collections.count)
        assertEquals(30, collections.totalCount)
    }

    @Test
    fun `test get collections with user id set to other owner return correct elements`() {
        collectionRepository.saveAll(List(30) {
            CollectionEntity(null, 1L, "collection-$it", arrayListOf())
        })
        collectionRepository.saveAll(List(30) {
            CollectionEntity(null, 2L, "collection-$it", arrayListOf())
        })
        val collections = collectionService.getCollections(0, 3L)
        assertEquals(0, collections.count)
        assertEquals(0, collections.totalCount)
    }

    @Test
    fun `test create collection`() {
        val col = collectionService.createCollection("foo", 1L)
        val ent = collectionRepository.getCollectionById(col.id)
        assertNotNull(ent)
        assertEquals(ent.id, col.id)
        assertEquals(ent.ownerId, col.userId)
        assertEquals(ent.name, col.name)
        assertEquals(0, col.cards.size)
    }

    @Test
    fun `test edit existing collection change the name`() {
        val ent = collectionRepository.save(
            CollectionEntity(
                null, 1L, "foo", listOf()
            )
        )
        assertEquals(ent.name, "foo")
        collectionService.editCollection(ent.id!!, "bar")

        val newEnt = collectionRepository.getCollectionById(ent.id!!)
        assertNotNull(newEnt)
        assertEquals(ent.ownerId, newEnt.ownerId)
        assertEquals("bar", newEnt.name)
    }

    @Test
    fun `test edit not existing collection throw CollectionNotFoundException`() {
        assertThrows<CollectionNotFoundException> {
            collectionService.editCollection(1L, "bar")
        }
    }

    @Test
    fun `test remove collection`() {
        val ent = collectionRepository.save(
            CollectionEntity(
                null, 1L, "foo", listOf()
            )
        )
        var otherEnt: CollectionEntity? = collectionRepository.save(
            CollectionEntity(
                null, 2L, "bar", listOf()
            )
        )
        val id = otherEnt?.id!!

        collectionService.removeCollection(ent.id!!)

        otherEnt = collectionRepository.getCollectionById(id)
        assertNotNull(otherEnt)
        assertEquals(id, otherEnt.id)
        assertEquals("bar", otherEnt.name)
        assertEquals(2L, otherEnt.ownerId)
    }
}