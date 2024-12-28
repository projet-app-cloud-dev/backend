package fr.pokecloud.cards.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity(name = "API_CARD")
data class Card(
    @Id
    @Column(name = "id")
    var id: Int,
    @Column(name = "name")
    val name: String,
    @Column(name = "image_url")
    val imageUrl: String,
)