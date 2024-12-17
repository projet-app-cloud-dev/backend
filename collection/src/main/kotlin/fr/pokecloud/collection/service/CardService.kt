package fr.pokecloud.collection.service

import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.model.Card
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class CardService(
    private val cardRepository: CardRepository
) {
    data class CardName(
        val id: Int, val name: String
    )

    private val restClient = RestClient.builder().baseUrl("http://cards").build()

    fun getOrInsertCard(cardId: Long): Card {
        val card = cardRepository.findByIdOrNull(cardId)?.let {
            Card(it.id, it.name)
        }
        if (card != null) {
            return card
        }

        val cardName = restClient.get().uri("/${cardId}").accept(MediaType.APPLICATION_JSON).retrieve()
            .body(CardName::class.java)!!.name
        return cardRepository.save(
            fr.pokecloud.collection.database.entities.Card(
                cardId, cardName, listOf()
            )
        ).let {
            Card(it.id, it.name)
        }
    }
}