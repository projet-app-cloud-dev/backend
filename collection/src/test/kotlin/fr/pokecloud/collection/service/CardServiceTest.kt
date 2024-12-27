package fr.pokecloud.collection.service

import com.fasterxml.jackson.databind.ObjectMapper
import fr.pokecloud.collection.database.repository.CardRepository
import fr.pokecloud.collection.model.exceptions.CardApiException
import fr.pokecloud.collection.model.exceptions.CardNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import fr.pokecloud.collection.database.entities.Card as CardEntity


@SpringBootTest
@AutoConfigureMockRestServiceServer
class CardServiceTest {
    @Autowired
    private lateinit var cardRepository: CardRepository


    @Autowired
    private lateinit var cardService: CardService


    @Autowired
    private lateinit var mockServer: MockRestServiceServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        cardRepository.deleteAll()
    }

    @Test
    fun `test get card already in db is ok`() {
        val cardEntity = cardRepository.save(CardEntity(1, "foo", listOf()))
        val card = cardService.getOrInsertCard(1)
        assertEquals(cardEntity.id, card.id)
        assertEquals(cardEntity.name, card.name)
    }

    @Test
    fun `test get card with api is ok`() {
        val responseBody = objectMapper.writeValueAsString(CardService.CardName(1, "foo"))
        mockServer.expect(requestTo("http://cards:8080/1"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON))
        val card = cardService.getOrInsertCard(1)
        assertEquals(1, card.id)
        assertEquals("foo", card.name)
        val cardEntity = cardRepository.findByIdOrNull(1L)
        assertNotNull(cardEntity)
        assertEquals(1, cardEntity.id)
        assertEquals("foo", cardEntity.name)
    }

    @Test
    fun `test get or insert card when api not found return not found`() {
        mockServer.expect(requestTo("http://cards:8080/1")).andExpect(method(HttpMethod.GET)).andRespond(
            withResourceNotFound(),
        )
        assertThrows<CardNotFoundException> { cardService.getOrInsertCard(1) }
    }

    @Test
    fun `test get or insert card when api return empty body return internal server error`() {
        mockServer.expect(requestTo("http://cards:8080/1")).andExpect(method(HttpMethod.GET)).andRespond(
            withSuccess(),
        )
        assertThrows<CardApiException> { cardService.getOrInsertCard(1) }
    }

    @Test
    fun `test get or insert card when api crash return internal server error`() {
        mockServer.expect(requestTo("http://cards:8080/1")).andExpect(method(HttpMethod.GET)).andRespond(
            withServerError(),
        )
        assertThrows<CardApiException> { cardService.getOrInsertCard(1) }
    }
}