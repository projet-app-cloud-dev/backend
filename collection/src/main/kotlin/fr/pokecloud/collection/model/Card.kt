package fr.pokecloud.collection.model

data class Card(
    val id: Long, val name: String,
)


data class CardCount(
    val id: Long, val name: String, val count : Long
)

data class SetCard(val cardId: Long, val cardCount : Long)