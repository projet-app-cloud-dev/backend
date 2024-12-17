package fr.pokecloud.collection.service

import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.database.repository.CollectionRepository
import fr.pokecloud.collection.model.Card
import fr.pokecloud.collection.model.Collection
import fr.pokecloud.collection.model.CollectionIdAndName
import fr.pokecloud.collection.model.CollectionList
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import fr.pokecloud.collection.database.entities.Collection as DataCollection

@Service
class CollectionService(
    private val collectionRepository: CollectionRepository, private val cardRepository: CardRepository
) {
    fun getCollection(id: Long): Collection? = collectionRepository.findByIdOrNull(
        id
    )?.let {
        Collection(id, it.ownerId, it.name, it.cards.map { card -> Card(card.card.id, card.card.name, card.count) })
    }

    fun getCollections(page: Int, userId: Long?): CollectionList {
        val pageable = Pageable.ofSize(25).withPage(page)
        val pageResult = if (userId != null) {
            collectionRepository.getCollectionsByOwnerId(userId, pageable)
        } else {
            collectionRepository.findAll(pageable)
        }

        return CollectionList(
            pageResult.content.map {
                CollectionIdAndName(
                    it.ownerId,
                    it.name,
                )
            }, pageResult.size, pageResult.totalElements
        )
    }

    fun createCollection(collectionName: String, ownerId: Long): Collection {
        val newCollection = collectionRepository.save(
            DataCollection(
                null, ownerId, collectionName, listOf()
            )
        )
        return Collection(newCollection.id!!, ownerId, collectionName, listOf())
    }

    fun editCollection(collectionId: Long, collectionName: String): Collection {
        return collectionRepository.save((collectionRepository.getCollectionById(collectionId) ?: TODO()).apply {
            name = collectionName
        }).let {
            Collection(
                collectionId, it.ownerId, it.name, it.cards.map { card -> Card(card.card.id, card.card.name, card.count) })
        }

    }

    fun removeCollection(collectionId: Long) {
        collectionRepository.deleteById(collectionId)
    }

    fun addCard(collectionId: Long, cardId: Long) {
        TODO()
    }
}