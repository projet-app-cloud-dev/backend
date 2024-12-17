package fr.pokecloud.collection.database.entities

import jakarta.persistence.*

@Entity
data class Collection(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false) var ownerId: Long,
    @Column(nullable = false) var name: String,

    @ManyToMany
    val cards: List<CardCollection>
)
