package fr.pokecloud.collection.model

data class Card(
    val id: Long, val name: String, val count: Long
)

data class AddCard(val id: Long)