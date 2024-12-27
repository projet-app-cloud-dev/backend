package fr.pokecloud.cards.api

import com.fasterxml.jackson.databind.ObjectMapper
import fr.pokecloud.cards.api.model.ApiCard
import fr.pokecloud.cards.api.model.ApiImage
import fr.pokecloud.cards.api.model.ApiResponse
import fr.pokecloud.cards.api.model.exception.ApiException
import fr.pokecloud.cards.database.CardRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilderFactory
import kotlin.test.assertContentEquals

@SpringBootTest
@AutoConfigureMockRestServiceServer
class ApiServiceTest {

    @Autowired
    private lateinit var mockServer: MockRestServiceServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var apiService: ApiService

    @Autowired
    private lateinit var cardRepository: CardRepository

    private val uriBuilderFactory: UriBuilderFactory = DefaultUriBuilderFactory()

    @Value("\${image.apiKey}")
    private lateinit var apiKey: String

    @Value("\${image.baseUrl}")
    private lateinit var baseUrl: String

    @BeforeEach
    fun setup() {
        cardRepository.deleteAll()
    }

    @Test
    fun `test api return ok with body return body`() {
        val body = ApiResponse(List(50) { ApiCard("card-$it", "card $it", ApiImage("image-$it")) })
        val bodyString = objectMapper.writeValueAsString(body)
        val nameQuery = "eevee"
        val page = 0
        val uri = uriBuilderFactory.uriString(baseUrl).path("/v2/cards").queryParam("q", "name:$nameQuery")
            .queryParam("page", page + 1).queryParam("pageSize", 50).queryParam("select", "id,name,images").build()
        mockServer.expect(requestTo(uri)).andExpect(method(HttpMethod.GET)).andExpect(header("X-Api-Key", apiKey))
            .andRespond(withSuccess(bodyString, MediaType.APPLICATION_JSON))
        val list = apiService.getCards(nameQuery, page)
        assertContentEquals(body.data, list)
    }

    @Test
    fun `test api return ok empty body throws api exception`() {
        val nameQuery = "eevee"
        val page = 0
        val uri = uriBuilderFactory.uriString(baseUrl).path("/v2/cards").queryParam("q", "name:$nameQuery")
            .queryParam("page", page + 1).queryParam("pageSize", 50).queryParam("select", "id,name,images").build()
        mockServer.expect(requestTo(uri)).andExpect(method(HttpMethod.GET)).andExpect(header("X-Api-Key", apiKey))
            .andRespond(withSuccess())
        assertThrows<ApiException> {
            apiService.getCards(nameQuery, page)
        }
    }

    @Test
    fun `test api return error throws api exception`() {
        val nameQuery = "eevee"
        val page = 0
        val uri = uriBuilderFactory.uriString(baseUrl).path("/v2/cards").queryParam("q", "name:$nameQuery")
            .queryParam("page", page + 1).queryParam("pageSize", 50).queryParam("select", "id,name,images").build()
        mockServer.expect(requestTo(uri)).andExpect(method(HttpMethod.GET)).andExpect(header("X-Api-Key", apiKey))
            .andRespond(withServerError())
        assertThrows<ApiException> {
            apiService.getCards(nameQuery, page)
        }
    }
}