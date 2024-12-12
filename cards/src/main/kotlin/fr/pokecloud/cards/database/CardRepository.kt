package fr.pokecloud.cards.database

import org.springframework.data.jpa.repository.JpaRepository

interface CardRepository : JpaRepository<Card, Int>