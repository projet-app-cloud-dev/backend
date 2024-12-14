package fr.pokecloud.cards

import fr.pokecloud.cards.api.ApiService
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class CardsApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    val apiService: ApiService = mock(ApiService::class.java)

    @Test
    fun `test get same page with same name return same value`() {
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
}
