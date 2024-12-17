package fr.pokecloud.collection.service

import fr.pokecloud.collection.database.entities.CardCollection
import fr.pokecloud.collection.database.entities.CardCollectionId
import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.database.repository.CollectionCardRepository
import fr.pokecloud.collection.database.repository.CollectionRepository
import fr.pokecloud.collection.model.Card
import fr.pokecloud.collection.model.CardCount
import fr.pokecloud.collection.model.Collection
import fr.pokecloud.collection.model.CollectionIdAndName
import fr.pokecloud.collection.model.CollectionList
import fr.pokecloud.collection.model.exceptions.CardNotFoundException
import fr.pokecloud.collection.model.exceptions.CollectionNotFoundException
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import fr.pokecloud.collection.database.entities.Collection as DataCollection

@Service
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val cardRepository: CardRepository,
    private val collectionCardRepository: CollectionCardRepository
) {
    fun getCollection(id: Long): Collection? = collectionRepository.findByIdOrNull(
        id
    )?.let {
        Collection(
            id, it.ownerId, it.name, it.cards.map { card -> CardCount(card.card.id, card.card.name, card.count) })
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
                collectionId,
                it.ownerId,
                it.name,
                it.cards.map { card -> CardCount(card.card.id, card.card.name, card.count) })
        }

    }

    fun removeCollection(collectionId: Long) {
        collectionRepository.deleteById(collectionId)
    }

    fun setCard(collection: Collection, card: Card, count: Long) {
        val dbCollection = collectionRepository.findByIdOrNull(collection.id) ?: throw CollectionNotFoundException()
        val dbCard = cardRepository.findByIdOrNull(card.id) ?: throw CardNotFoundException()

        val cardCollection = collectionCardRepository.findByIdOrNull(CardCollectionId(dbCard, dbCollection))
            ?: collectionCardRepository.save(
                CardCollection(dbCard, dbCollection, 0L)
            )

        cardCollection.count = count

        collectionCardRepository.save(cardCollection)
    }

    fun removeCard(collection: Collection, cardId: Long) {
        val dbCollection = collectionRepository.findByIdOrNull(collection.id) ?: throw CollectionNotFoundException()
        val dbCard = cardRepository.findByIdOrNull(cardId) ?: throw CardNotFoundException()

        val cardCollection = collectionCardRepository.findByIdOrNull(CardCollectionId(dbCard, dbCollection)) ?: return
        collectionCardRepository.delete(cardCollection)
    }
}