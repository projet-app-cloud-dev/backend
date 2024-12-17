package fr.pokecloud.collection.database.repository

import fr.pokecloud.collection.database.entities.Collection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CollectionRepository : JpaRepository<Collection, Long> {
    fun getCollectionById(id: Long): Collection?

    fun getCollectionsByOwnerId(ownerId: Long, pageable: Pageable): Page<Collection>
}