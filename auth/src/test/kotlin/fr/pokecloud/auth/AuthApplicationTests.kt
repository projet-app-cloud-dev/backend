package fr.pokecloud.auth


import com.fasterxml.jackson.databind.ObjectMapper
import fr.pokecloud.auth.dto.AccountInformations
import fr.pokecloud.auth.dto.UsernameAndPassword
import fr.pokecloud.auth.model.User
import fr.pokecloud.auth.service.PasswordService
import fr.pokecloud.auth.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AuthApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    val passwordService = mock(PasswordService::class.java)

    @MockitoBean
    val userService = mock(UserService::class.java)

    @Test
    fun testLogin() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        val user = User(0, username, password)

        doReturn(user).`when`(userService).getUserByUsername(username)
        doReturn(true).`when`(passwordService).checkPassword(password, password)

        mvc.perform(
            post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isOk()).andExpect(header().exists("Authorization"))
    }

    // TODO: more tests

    @Test
    fun testSignupIsOk() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        val user = User(0, username, password)

        doReturn(user).`when`(userService).createUser(username, password)

        mvc.perform(
            post("/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isOk()).andExpect(header().exists("Authorization"))
    }


    @Test
    fun testGetUser() {
        val user = User(1, "username", "password")
        doReturn(user).`when`(userService).getUserById(1)
        mvc.perform(
            get("/info/1")

        ).andExpect(status().isOk()).andExpect(content().json(objectMapper.writeValueAsString(AccountInformations("username"))))
    }
}
