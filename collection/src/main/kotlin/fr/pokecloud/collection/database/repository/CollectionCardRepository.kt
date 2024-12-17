package fr.pokecloud.collection.database.repository

import fr.pokecloud.collection.database.entities.CardCollection
import fr.pokecloud.collection.database.entities.CardCollectionId
import org.springframework.data.jpa.repository.JpaRepository

interface CollectionCardRepository : JpaRepository<CardCollection, CardCollectionId>