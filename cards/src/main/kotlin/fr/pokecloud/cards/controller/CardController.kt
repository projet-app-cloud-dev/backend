package fr.pokecloud.cards.controller

import fr.pokecloud.cards.database.CardRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
@CrossOrigin
class CardController(
    val cardRepository: CardRepository
) {
    data class CardInformations(
        val id: Int, val name: String, val imageUrl: String
    )

    @GetMapping("/{id}")
    @Operation(description = "Get card informations.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "The requested card.", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = CardInformations::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "The card was not found on the local database.",
                content = [Content()]
            ),
        ]
    )
    fun getCardName(@PathVariable("id") id: Int): ResponseEntity<CardInformations> {
        val card = cardRepository.findByIdOrNull(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CardInformations(card.id, card.name, card.imageUrl))
    }

    @GetMapping("/")
    @Operation(description = "Search a card using his name.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "A card list.", content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = CardInformations::class))
                )]
            ),
            ApiResponse(
                responseCode = "500", description = "Failed to fetch the api.", content = [Content()]
            ),
        ]
    )
    fun searchCards(query: String, page: Int = 0): ResponseEntity<List<CardInformations>> {
        val repo = cardRepository.findCardsByNameContainingIgnoreCase(
            query, Pageable.ofSize(100).withPage(page)
        ).content.map {
            CardInformations(it.id, it.name, it.imageUrl)
        }
        return ResponseEntity.ok(repo)
    }

    @GetMapping("/{id}/image")
    @Operation(description = "Redirect to card image.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "The card image", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = CardInformations::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "The card was not found on the local database.",
                content = [Content()]
            ),
        ]
    )
    fun getCardImage(@PathVariable("id") id: Int): ResponseEntity<Void> {
        val card = cardRepository.findByIdOrNull(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, card.imageUrl).build()
    }
}