package fr.pokecloud.collection.database.repository

import fr.pokecloud.collection.database.entities.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, Long> {
}