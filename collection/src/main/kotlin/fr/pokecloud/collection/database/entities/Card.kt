package fr.pokecloud.collection.database.entities

import jakarta.persistence.*

@Entity
data class Card(
    @Id val id: Long, @Column(nullable = false) val name: String,

    @ManyToMany private val collections: List<CardCollection>
)

@Entity
data class CardCollection(
    @ManyToOne
    @Id
    val card: Card,
    @ManyToOne
    @Id
    val collection: Collection,
    @Column(nullable = false) val count: Long

)