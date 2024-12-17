package fr.pokecloud.collection.model

data class Collection(
    val id: Long,
    val userId: Long, val name: String, val cards: List<CardCount>
)

data class CollectionList(
    val collections: List<CollectionIdAndName>,
    val count: Int,
    val totalCount: Long,
)

data class CollectionName(
    val name: String,
)

data class CollectionIdAndName(val id: Long, val name: String)