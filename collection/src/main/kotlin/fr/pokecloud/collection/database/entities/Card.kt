package fr.pokecloud.collection.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import java.io.Serializable

@Entity
data class Card(
    @Id val id: Long, @Column(nullable = false) val name: String,

    @ManyToMany private val collections: List<CardCollection>
)

data class CardCollectionId(
    val card: Card, val collection: Collection
) : Serializable

@Entity
@IdClass(CardCollectionId::class)
data class CardCollection(
    @ManyToOne @Id val card: Card, @ManyToOne @Id val collection: Collection, @Column(nullable = false) var count: Long
)