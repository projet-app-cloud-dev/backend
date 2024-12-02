package fr.pokecloud.collection.model

data class Collection(
    val userId: Long, val name: String, val cards: List<Card>
)

data class CollectionList(
    val collections: List<Collection>,
    val totalCount: Int,
    val lastId: Long?
)

data class CollectionName(
    val name: String,
)