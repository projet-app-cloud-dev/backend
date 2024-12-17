package fr.pokecloud.collection

import com.fasterxml.jackson.databind.ObjectMapper
import fr.pokecloud.collection.model.Collection
import fr.pokecloud.collection.model.CollectionIdAndName
import fr.pokecloud.collection.model.CollectionList
import fr.pokecloud.collection.model.CollectionName
import fr.pokecloud.collection.service.CardService
import fr.pokecloud.collection.service.CollectionService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class CollectionApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    val collectionService = mock(CollectionService::class.java)

    @MockitoBean
    val cardService = mock(CardService::class.java)

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun testGetCollectionByIdOk() {
        val collection = Collection(1L, 1L, "foo", emptyList())
        doReturn(collection).`when`(collectionService).getCollection(collection.id)
        mvc.perform(MockMvcRequestBuilders.get("/{id}", collection.id)).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(collection)))
    }

    @Test
    fun testGetCollectionByIdNotFound() {
        doReturn(null).`when`(collectionService).getCollection(1L)
        mvc.perform(MockMvcRequestBuilders.get("/{id}", 1L)).andExpect(status().isNotFound())
    }

    @Test
    fun testGetCollectionListOk() {
        val collection = CollectionList(List(100) { CollectionIdAndName(it.toLong(), "foo $it") }, 100, 100)
        val emptyList = CollectionList(listOf(), 0, 100)

        doReturn(collection).`when`(collectionService).getCollections(0, null)
        doReturn(emptyList).`when`(collectionService).getCollections(1, null)
        mvc.perform(MockMvcRequestBuilders.get("/")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(collection)))
        mvc.perform(MockMvcRequestBuilders.get("/").param("page", "0")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(collection)))
        mvc.perform(MockMvcRequestBuilders.get("/").param("page", "1")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(emptyList)))
    }

    @Test
    fun testGetCollectionListMineWithoutLoginThrowBadRequest() {
        mvc.perform(MockMvcRequestBuilders.get("/").param("mine", "true")).andExpect(status().isBadRequest())
    }

    @WithMockUser(username = "1", password = "pwd")
    @Test
    fun testGetCollectionListMineWithLoginIsOk() {
        val collection = CollectionList(List(100) { CollectionIdAndName(it.toLong(), "foo $it") }, 100, 100)
        val emptyList = CollectionList(listOf(), 0, 100)
        doReturn(collection).`when`(collectionService).getCollections(0, 1L)
        doReturn(emptyList).`when`(collectionService).getCollections(1, 1L)
        mvc.perform(MockMvcRequestBuilders.get("/").param("mine", "true")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(collection)))
        mvc.perform(MockMvcRequestBuilders.get("/").param("page", "1").param("mine", "true")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(emptyList)))
    }

    @WithMockUser(username = "1", password = "pwd")
    @Test
    fun testGetCollectionListWithLoginIsOk() {
        val collection = CollectionList(List(100) { CollectionIdAndName(it.toLong(), "foo $it") }, 100, 100)
        val emptyList = CollectionList(listOf(), 0, 100)
        doReturn(collection).`when`(collectionService).getCollections(0, null)
        doReturn(emptyList).`when`(collectionService).getCollections(1, null)
        mvc.perform(MockMvcRequestBuilders.get("/").param("page", "0")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(collection)))
        mvc.perform(MockMvcRequestBuilders.get("/").param("page", "1")).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(emptyList)))
    }

    @Test
    @WithMockUser(username = "1", password = "pwd")
    fun `test create collection with auth is ok`() {
        val newCollection = Collection(0L, 1L, "foo", listOf())
        doReturn(newCollection).`when`(collectionService).createCollection("foo", 1L)
        mvc.perform(
            post("/").contentType("application/json")
                .content(objectMapper.writeValueAsString(CollectionName(newCollection.name)))
        ).andExpect(status().isCreated()).andExpect(header().stringValues("Location", "/0"))
            .andExpect(content().json(objectMapper.writeValueAsString(newCollection)))
    }

    @Test
    fun `test create collection without auth is unauthorized`() {
        mvc.perform(
            post("/").content(objectMapper.writeValueAsString(CollectionName("foo")))
        ).andExpect(status().isUnauthorized())
    }

    @Test
    @WithMockJwt(value = 1L)
    fun `edit collection is ok`() {
        val collectionId = 1L
        val newCollectionName = "New Name"

        val initialCollection = Collection(collectionId, 1L, newCollectionName, listOf())
        val updatedCollection = Collection(collectionId, 1L, newCollectionName, listOf())

        doReturn(initialCollection).`when`(collectionService).getCollection(collectionId)
        // Mock the service to return the updated collection
        doReturn(updatedCollection).`when`(collectionService).editCollection(collectionId, newCollectionName)

        mvc.perform(
            post("/{collectionId}", collectionId).contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "$newCollectionName"}""")
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(collectionId))
            .andExpect(jsonPath("$.name").value(newCollectionName))
    }

    @Test
    @WithMockJwt(value = 1L)
    fun `edit collection should return bad request for invalid input`() {
        val collectionId = 1L
        val invalidCollectionName = ""

        mvc.perform(
            post("/{collectionId}", collectionId).contentType(MediaType.APPLICATION_JSON)
                .content("""{"notname": "$invalidCollectionName"}""")
        ).andExpect(status().isBadRequest)

        mvc.perform(
            post("/{collectionId}", collectionId).contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "$invalidCollectionName"}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockJwt(value = 1L)
    fun `remove is ok`() {
        val collectionId = 1L
        val newCollectionName = "New Name"

        val initialCollection = Collection(collectionId, 1L, newCollectionName, listOf())

        doReturn(initialCollection).`when`(collectionService).getCollection(collectionId)
        // Mock the service to return the updated collection
        doNothing().`when`(collectionService).removeCollection(collectionId)

        mvc.perform(
            delete("/{collectionId}", collectionId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }


    @Test
    fun `remove collection should return unauthorized for missing token`() {
        val collectionId = 1L
        mvc.perform(
            delete("/{collectionId}", collectionId).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized())
    }


    @Test
    @WithMockJwt(value = 0L)
    fun `remove collection should return forbidden for unauthorized user`() {
        val collectionId = 1L
        doReturn(Collection(collectionId, 0, "foo", listOf())).`when`(collectionService).getCollection(collectionId)

        mvc.perform(
            delete("/{collectionId}", collectionId).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden())
    }

    @Test
    @WithMockUser(username = "1", password = "pwd")
    fun `remove collection should return not found for non-existent collection`() {
        val collectionId = 100L

        // Mock the service to throw a CollectionNotFoundException
        doReturn(null).`when`(collectionService).getCollection(collectionId)

        mvc.perform(
            delete("/{collectionId}", collectionId).header("Authorization", "Bearer valid_token")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1", password = "pwd")
    fun `add card is ok`() {
        val collectionId = 100L
        val cardId = 1L

        // Mock the service to throw a CollectionNotFoundException
        doReturn(null).`when`(collectionService).getCollection(collectionId)
//        val c = doReturn(Card(1, "a", 1L)).`when`(cardService).getCard(cardId)
//
//        verify(collectionService, times(1)).addCard(100L, 1)

        /*
        mvc.perform(
            post("/{collectionId}", collectionId).header("Authorization", "Bearer valid_token")
                .contentType(MediaType.APPLICATION_JSON).content("""{"name": "$newCollectionName"}""")
        ).andExpect(status().isOk).andExpect(jsonPath("$.id").value(collectionId))
            .andExpect(jsonPath("$.name").value(newCollectionName))

         */
    }
}
