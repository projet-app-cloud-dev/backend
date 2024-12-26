package fr.pokecloud.cards.api

import fr.pokecloud.cards.api.model.ApiCard
import fr.pokecloud.cards.api.model.ApiResponse
import fr.pokecloud.cards.api.model.exception.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ApiService(@Value("\${image.apiKey}") apiKey: String, @Value("\${image.baseUrl}") baseUrl: String) {
    private val restClient = RestClient.builder().defaultHeader("X-Api-Key", apiKey).baseUrl(baseUrl).build()

    @Throws(ApiException::class)
    fun getCards(nameQuery: String, page: Int): List<ApiCard> {
        try {
            val response = restClient.get().uri("/v2/cards") { uriBuilder ->
                uriBuilder.queryParam("q", "name:$nameQuery").queryParam("page", page + 1).queryParam("pageSize", 50)
                    .queryParam("select", "id,name,images").build()
            }.retrieve()
            val cards = response.toEntity(ApiResponse::class.java).body ?: throw ApiException("Missing body")
            return cards.data
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }
}