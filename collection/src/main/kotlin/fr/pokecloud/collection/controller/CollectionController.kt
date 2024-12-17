package fr.pokecloud.collection.controller

import fr.pokecloud.collection.model.Collection
import fr.pokecloud.collection.model.CollectionList
import fr.pokecloud.collection.model.CollectionName
import fr.pokecloud.collection.model.SetCard
import fr.pokecloud.collection.model.exceptions.CollectionNotFoundException
import fr.pokecloud.collection.service.CardService
import fr.pokecloud.collection.service.CollectionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.net.URI

@RestController
class CollectionController(
    private val collectionService: CollectionService, private val cardService: CardService
) {
    @GetMapping("/")
    @Operation(description = "Get a list of collections.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = CollectionList::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid request.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "No collection was found.", content = [Content()]
            ),
        ]
    )
    fun getCollections(
        page: Int = 0, mine: Boolean = false, auth: Authentication? = null
    ): ResponseEntity<CollectionList> {
        val collection = if (mine) {
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
            } else {
                collectionService.getCollections(page, auth.name.toLong())
            }
        } else {
            collectionService.getCollections(page, null)
        }

        return ResponseEntity.ok(collection)
    }

    @PostMapping("/")
    @Operation(description = "Create a new collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Collection created successfully.s", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = Collection::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Bad request.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "401", description = "Missing authentication token.", content = [Content()]
            ),
        ]
    )
    fun createCollection(
        @RequestBody collectionName: CollectionName, auth: Authentication
    ): ResponseEntity<Collection> {
        val collection = collectionService.createCollection(collectionName.name, auth.name.toLong())
        return ResponseEntity.created(
            URI.create(
                MvcUriComponentsBuilder.fromMappingName("getCollection").arg(0, collection.id).build()
            )
        ).body(collection)
    }

    @GetMapping("/{collectionId}", name = "getCollection")
    @Operation(description = "Get a collection.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = Collection::class)
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "No collection was found.", content = [Content()]
            ),
        ]
    )
    fun getCollection(@PathVariable collectionId: Long): ResponseEntity<Collection> =
        collectionService.getCollection(collectionId)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound()
            .build()

    @PostMapping("/{collectionId}")
    @Operation(description = "Edit a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Collection edited successfully.s", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = Collection::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Bad request.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "You don't have the right to do that or missing authentication token.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection does not exist", content = [Content()]
            ),
        ]
    )
    fun editCollection(
        @PathVariable collectionId: Long, @RequestBody collectionName: CollectionName, auth: Authentication
    ): ResponseEntity<Collection> {
        return if (collectionName.name.isBlank()) {
            ResponseEntity.badRequest().build()
        } else {
            val collection = collectionService.getCollection(collectionId)
            return if (collection != null) {
                val userId = auth.name.toLong()
                if (collection.id == userId) {
                    val col = collectionService.editCollection(collectionId, collectionName.name)
                    ResponseEntity.ok(col)
                } else {
                    ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                }
            } else {
                ResponseEntity.notFound().build()
            }
        }
    }

    @DeleteMapping("/{collectionId}")
    @Operation(description = "Remove a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "Collection deleted", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "You don't have the right to do that or authentication token is missing.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection does not exist", content = [Content()]
            ),
        ]
    )
    fun removeCollection(
        @PathVariable collectionId: Long, auth: Authentication
    ): ResponseEntity<Void> {
        val collection = collectionService.getCollection(collectionId)
        return if (collection != null) {
            val userId = auth.name.toLong()
            if (collection.id == userId) {
                collectionService.removeCollection(collectionId)
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{collectionId}/cards")
    @Operation(description = "Set a card in a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Card added to the collection", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "You don't have the right to do that or authentication token is missing..",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection or card does not exist", content = [Content()]
            ),
        ]
    )
    fun setCard(@PathVariable collectionId: Long, setCard: SetCard, auth: Authentication): ResponseEntity<Void> {
        val card = cardService.getOrInsertCard(setCard.cardId)
        val collection = collectionService.getCollection(collectionId) ?: throw CollectionNotFoundException()
        if (collection.userId != auth.name.toLong()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        collectionService.setCard(collection, card, setCard.cardCount)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{collectionId}/cards/{cardId}")
    @Operation(description = "Remove a card from a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Card removed from the collection", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "You don't have the right to do that or missing authentication token.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection or card does not exist", content = [Content()]
            ),
        ]
    )
    fun removeCard(
        @PathVariable collectionId: Long, @PathVariable cardId: Long, auth: Authentication
    ): ResponseEntity<Void> {
        val collection = collectionService.getCollection(collectionId) ?: throw CollectionNotFoundException()
        if (collection.userId != auth.name.toLong()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        collectionService.removeCard(collection, cardId)
        return ResponseEntity.ok().build()
    }
}