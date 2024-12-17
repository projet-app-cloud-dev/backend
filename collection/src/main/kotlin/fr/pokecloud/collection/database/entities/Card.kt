package fr.pokecloud.collection.database.entities

import jakarta.persistence.*
import java.io.Serializable

@Entity
data class Card(
    @Id val id: Long, @Column(nullable = false) val name: String,

    @OneToMany(
        mappedBy = "card", cascade = [CascadeType.ALL], orphanRemoval = true
    ) private val collections: List<CardCollection>
)


class CardCollectionId() : Serializable {
    private lateinit var card: Card
    private lateinit var collection: Collection

    constructor(card: Card, collection: Collection) : this() {
        this.card = card
        this.collection = collection
    }
}

@Entity
@IdClass(CardCollectionId::class)
data class CardCollection(
    @ManyToOne @Id val card: Card, @ManyToOne @Id val collection: Collection, @Column(nullable = false) var count: Long
)