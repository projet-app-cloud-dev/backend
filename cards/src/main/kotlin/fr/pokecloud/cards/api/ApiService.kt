package fr.pokecloud.cards.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ApiService(@Value("\${image.apiKey}") apiKey: String, @Value("\${image.baseUrl}") baseUrl: String) {
    class ApiResponse(
        val data: List<ApiCard>
    )

    class ApiCard(
        val id: String, val name: String, val images: Images
    )

    class Images(
        val large: String,
    )

    private val restClient = RestClient.builder().defaultHeader("X-Api-Key", apiKey).baseUrl(baseUrl).build()


    fun getCards(nameQuery: String, page: Int): List<ApiCard> {
        val response = restClient.get().uri("/v2/cards") { uriBuilder ->
            uriBuilder.queryParam("q", "name:$nameQuery").queryParam("page", page + 1).queryParam("pageSize", 50)
                .queryParam("select", "id,name,images").build()
        }.retrieve()
        val cards = requireNotNull(response.toEntity(ApiResponse::class.java).body) {
            "Missing response body"
        }
        return cards.data
    }
}