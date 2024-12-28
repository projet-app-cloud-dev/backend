package fr.pokecloud.cards

import fr.pokecloud.cards.database.Card
import fr.pokecloud.cards.database.CardRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class CardsApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var cardRepository: CardRepository

    @BeforeEach
    fun setup() {
        cardRepository.deleteAll()
    }

    @Test
    fun `test getCardName with good id return correct value`() {
        cardRepository.save(Card(1, "foo", "bar"))
        mvc.perform(MockMvcRequestBuilders.get("/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("foo"))
    }

    @Test
    fun `test getCardName with bad id return not found`() {
        mvc.perform(MockMvcRequestBuilders.get("/1")).andExpect(status().isNotFound())
    }

    @Test
    fun `test getCardImage with good id redirect to correct location`() {
        cardRepository.save(Card(1, "foo", "bar"))
        mvc.perform(MockMvcRequestBuilders.get("/1/image")).andExpect(status().isMovedPermanently())
            .andExpect(header().string("Location", "bar"))
    }

    @Test
    fun `test getCardImage with bad id returns not found`() {
        mvc.perform(MockMvcRequestBuilders.get("/1/image")).andExpect(status().isNotFound())
    }
}
