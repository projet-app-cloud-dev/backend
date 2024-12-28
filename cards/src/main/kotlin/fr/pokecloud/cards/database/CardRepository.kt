package fr.pokecloud.cards.database

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CardRepository : JpaRepository<Card, Int> {
    fun findCardsByNameContainingIgnoreCase(query: String, page: Pageable): Page<Card>
}