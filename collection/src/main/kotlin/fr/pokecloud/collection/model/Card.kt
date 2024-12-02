package fr.pokecloud.collection.model

data class Card(
    val cardId: Long,
    val cardName: String,
    val cardImageUrl: String,
)

data class AddCard(val cardId: Long)