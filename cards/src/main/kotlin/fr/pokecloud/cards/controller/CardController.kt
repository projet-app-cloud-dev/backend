package fr.pokecloud.cards.controller

import fr.pokecloud.cards.api.ApiService
import fr.pokecloud.cards.database.Card
import fr.pokecloud.cards.database.CardRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class CardController(
    val apiService: ApiService, val cardRepository: CardRepository
) {
    data class CardName(
        val id: Int, val name: String
    )

    @GetMapping("/{id}")
    fun getCardName(@PathVariable("id") id: Int): ResponseEntity<CardName> {
        val card = cardRepository.findByIdOrNull(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CardName(card.id, card.name))
    }

    @GetMapping("/")
    fun searchCards(query: String, page: Int = 0): ResponseEntity<List<CardName>> {
        val repo = cardRepository.saveAll(apiService.getCards(query, page).map {
            Card(it.id.hashCode(), it.id, it.name, it.images.large)
        }).map {
            CardName(it.id, it.name)
        }
        return ResponseEntity.ok(repo)
    }

    @GetMapping("/{id}/image")
    fun getCardImage(@PathVariable("id") id: Int): ResponseEntity<Void> {
        val card = cardRepository.findByIdOrNull(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, card.imageUrl).build()
    }
}