package fr.pokecloud.collection.controller

import fr.pokecloud.collection.model.AddCard
import fr.pokecloud.collection.model.Collection
import fr.pokecloud.collection.model.CollectionList
import fr.pokecloud.collection.model.CollectionName
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
class CollectionController {
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
                responseCode = "404", description = "No collection was found.", content = [Content()]
            ),
        ]
    )
    fun getCollections(afterId: Long?, mine: Boolean = true): ResponseEntity<CollectionList> = TODO()

    @PostMapping("/")
    @Operation(description = "Create a new collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Collection created successfully.s", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = CollectionList::class)
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
    ): ResponseEntity<Collection> = TODO()

    @GetMapping("/{collectionId}")
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
    fun getCollection(@PathVariable collectionId: Long): ResponseEntity<Collection> = TODO()

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
                responseCode = "401", description = "Missing authentication token.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "403", description = "You don't have the right to do that.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection does not exist", content = [Content()]
            ),
        ]
    )
    fun editCollection(
        @PathVariable collectionId: Long, @RequestBody collectionName: CollectionName, auth: Authentication
    ): ResponseEntity<Collection> = TODO()

    @DeleteMapping("/{collectionId}")
    @Operation(description = "Remove a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "Collection deleted", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "401", description = "Missing authentication token.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "403", description = "You don't have the right to do that.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection does not exist", content = [Content()]
            ),
        ]
    )
    fun removeCollection(
        @PathVariable collectionId: Long, auth: Authentication
    ): ResponseEntity<Void> = TODO()

    @PostMapping("/{collectionId}/cards")
    @Operation(description = "Add a card to a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Card added to the collection", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "401", description = "Missing authentication token.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "403", description = "You don't have the right to do that.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection does not exist", content = [Content()]
            ),
        ]
    )
    fun addCard(@PathVariable collectionId: Long, addCard: AddCard, auth: Authentication): ResponseEntity<Void> = TODO()

    @DeleteMapping("/{collectionId}/cards/{cardId}")
    @Operation(description = "Remove a card from a collection.", security = [SecurityRequirement(name = "auth_token")])
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Card removed from the collection", content = [Content(
                )]
            ),
            ApiResponse(
                responseCode = "401", description = "Missing authentication token.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "403", description = "You don't have the right to do that.", content = [Content()]
            ),
            ApiResponse(
                responseCode = "404", description = "Collection or card does not exist", content = [Content()]
            ),
        ]
    )
    fun removeCard(
        @PathVariable collectionId: Long, @PathVariable cardId: Long, auth: Authentication
    ): ResponseEntity<Void> = TODO()
}