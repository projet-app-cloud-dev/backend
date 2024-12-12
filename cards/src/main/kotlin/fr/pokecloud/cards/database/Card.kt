package fr.pokecloud.cards.database

import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity(name = "API_CARD")
data class Card(
    @Id var id: Int,
    val apiId: String,
    val name: String,
    val imageUrl: String,
)