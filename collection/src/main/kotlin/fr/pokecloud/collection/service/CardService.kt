package fr.pokecloud.collection.service

import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.model.Card
import fr.pokecloud.collection.model.exceptions.CardApiException
import fr.pokecloud.collection.model.exceptions.CardNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.toEntity
import fr.pokecloud.collection.database.entities.Card as CardEntity

@Service
class CardService(
    private val cardRepository: CardRepository, restClientBuilder: RestClient.Builder
) {
    data class CardName(
        val id: Int, val name: String
    )

    private val restClient = restClientBuilder.baseUrl("http://cards:8080").build()

    fun getOrInsertCard(cardId: Long): Card {
        val card = cardRepository.findByIdOrNull(cardId)?.let {
            Card(it.id, it.name)
        }
        if (card != null) {
            return card
        }

        try {
            val cardResponse =
                restClient.get().uri("/${cardId}").accept(MediaType.APPLICATION_JSON).retrieve().toEntity<Card>()
            val body = cardResponse.body ?: throw CardApiException("Missing response body")
            return cardRepository.save(CardEntity(cardId, body.name, listOf())).let {
                Card(it.id, it.name)
            }
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw CardNotFoundException()
            } else {
                throw CardApiException(e)
            }
        }
    }
}