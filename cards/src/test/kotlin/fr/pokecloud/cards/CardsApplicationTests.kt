package fr.pokecloud.cards

import fr.pokecloud.cards.api.ApiService
import fr.pokecloud.cards.database.Card
import fr.pokecloud.cards.database.CardRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.AdditionalMatchers.not
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class CardsApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    val apiService: ApiService = mock(ApiService::class.java)

    @Autowired
    lateinit var cardRepository: CardRepository

    @BeforeEach
    fun setup() {
        cardRepository.deleteAll()
    }

    @Test
    fun `test getCardName with good id return correct value`() {
        cardRepository.save(Card(1, "dp", "foo", "bar"))
        mvc.perform(MockMvcRequestBuilders.get("/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("foo"))
    }

    @Test
    fun `test getCardName with bad id return not found`() {
        mvc.perform(MockMvcRequestBuilders.get("/1")).andExpect(status().isNotFound())
    }

    @Test
    fun `test getCardImage with good id redirect to correct location`() {
        cardRepository.save(Card(1, "dp", "foo", "bar"))
        mvc.perform(MockMvcRequestBuilders.get("/1/image")).andExpect(status().isMovedPermanently())
            .andExpect(header().string("Location", "bar"))
    }

    @Test
    fun `test getCardImage with bad id returns not found`() {
        mvc.perform(MockMvcRequestBuilders.get("/1/image")).andExpect(status().isNotFound())
    }

    @Test
    fun `test search same page with same name return same value`() {
        val pkmList = List(10) {
            ApiService.ApiCard("e-$it", "eevee-$it", ApiService.Images("https://example.org"))
        }

        doReturn(pkmList).`when`(apiService).getCards("eevee", 0)

        val r = mvc.perform(MockMvcRequestBuilders.get("/").param("query", "eevee")).andExpect(status().isOk())
            .andReturn().response.contentAsString
        mvc.perform(MockMvcRequestBuilders.get("/").param("query", "eevee")).andExpect(status().isOk())
            .andExpect(content().json(r))

        verify(apiService, times(0)).getCards(anyString(), not(ArgumentMatchers.eq(0)))
    }

    // TODO: test api error return 500
}
