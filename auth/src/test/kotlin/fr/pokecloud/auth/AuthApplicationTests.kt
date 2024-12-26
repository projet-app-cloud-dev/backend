package fr.pokecloud.auth


import com.fasterxml.jackson.databind.ObjectMapper
import fr.pokecloud.auth.dto.AccountInformations
import fr.pokecloud.auth.dto.NewAccountInformations
import fr.pokecloud.auth.dto.NewPassword
import fr.pokecloud.auth.dto.UsernameAndPassword
import fr.pokecloud.auth.model.User
import fr.pokecloud.auth.model.exception.UsernameTakenException
import fr.pokecloud.auth.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AuthApplicationTests {
    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    val passwordEncoder = mock(PasswordEncoder::class.java)

    @MockitoBean
    val userService = mock(UserService::class.java)

    @Test
    fun `test login with good credentials is ok`() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        val user = User(0, username, password)

        doReturn(user).`when`(userService).getUserByUsername(username)
        doReturn(true).`when`(passwordEncoder).matches(password, password)

        mvc.perform(
            post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isOk()).andExpect(header().exists("Authorization"))
    }

    @Test
    fun `test login with bad username return unauthorized`() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        doReturn(null).`when`(userService).getUserByUsername(username)

        mvc.perform(
            post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isUnauthorized())
    }

    @Test
    fun `test login with bad password return unauthorized`() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        val user = User(0, username, password)

        doReturn(user).`when`(userService).getUserByUsername(username)
        doReturn(false).`when`(passwordEncoder).matches(password, password)

        mvc.perform(
            post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isUnauthorized())
    }

    @Test
    fun `test signup with valid credentials return ok`() {
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
    fun `test signup with already taken credentials return conflict`() {
        val username = "oupson"
        val password = "password"

        val loginDTO = UsernameAndPassword(username, password)

        doThrow(UsernameTakenException()).`when`(userService).createUser(username, password)

        mvc.perform(
            post("/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO))
        ).andExpect(status().isConflict())
    }

    @Test
    fun `test get existing user return said user`() {
        val user = User(1, "username", "password")
        doReturn(user).`when`(userService).getUserById(1)
        mvc.perform(
            get("/info/1")

        ).andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(AccountInformations("username"))))
    }

    @Test
    fun `test get non existing user return not found`() {
        doReturn(null).`when`(userService).getUserById(1)
        mvc.perform(
            get("/info/1")

        ).andExpect(status().isNotFound())
    }

    @Test
    fun `test edit user with username and password return ok`() {
        val userId = 1L
        val username = "test"
        val oldPassword = "password"
        val newPassword = "newPassword"

        val newInformationsDto = NewAccountInformations(username, NewPassword(oldPassword, newPassword))
        val user = User(userId, username, oldPassword)

        doReturn(user).`when`(userService).getUserById(userId)
        doReturn(true).`when`(passwordEncoder).matches(oldPassword, oldPassword)
        doNothing().`when`(userService).editUser(userId, username, newPassword)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isOk())
        verify(userService).editUser(userId, username, newPassword)
    }

    @Test
    fun `test edit user with username return ok`() {
        val userId = 1L
        val username = "test"

        val newInformationsDto = NewAccountInformations(username, null)
        val user = User(userId, username, "password")

        doReturn(user).`when`(userService).getUserById(userId)
        doNothing().`when`(userService).editUser(userId, username, null)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isOk())
        verify(userService).editUser(userId, username, null)
    }

    @Test
    fun `test edit user with password return ok`() {
        val userId = 1L
        val username = "test"
        val oldPassword = "old"
        val newPassword = "new"

        val newInformationsDto = NewAccountInformations(null, NewPassword(oldPassword, newPassword))
        val user = User(userId, username, oldPassword)

        doReturn(user).`when`(userService).getUserById(userId)
        doReturn(true).`when`(passwordEncoder).matches(oldPassword, oldPassword)
        doNothing().`when`(userService).editUser(userId, null, newPassword)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isOk())
        verify(userService).editUser(userId, null, newPassword)
    }


    @Test
    fun `test edit user without username and password return bad request`() {
        val newInformationsDto = NewAccountInformations(null, null)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isBadRequest())
    }

    @Test
    fun `test edit user with invalid userId return bad unauthorized`() {
        val newInformationsDto = NewAccountInformations("test", null)
        doReturn(null).`when`(userService).getUserById(1L)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isUnauthorized())
    }

    @Test
    fun `test edit user with invalid password return bad request`() {
        val userId = 1L
        val username = "test"
        val oldPassword = "old"
        val newPassword = "new"

        val newInformationsDto = NewAccountInformations(null, NewPassword(oldPassword, newPassword))
        val user = User(userId, username, oldPassword)

        doReturn(user).`when`(userService).getUserById(userId)
        doReturn(false).`when`(passwordEncoder).matches(oldPassword, oldPassword)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isBadRequest())
    }

    @Test
    fun `test edit user with taken username return conflict`() {
        val userId = 1L
        val username = "test"
        val oldPassword = "old"
        val newPassword = "new"
        val newInformationsDto = NewAccountInformations(username, NewPassword(oldPassword, newPassword))
        val user = User(userId, "old", oldPassword)
        doReturn(user).`when`(userService).getUserById(userId)
        doReturn(true).`when`(passwordEncoder).matches(oldPassword, oldPassword)
        doThrow(UsernameTakenException()).`when`(userService).editUser(userId, username, newPassword)
        mvc.perform(
            put("/info").contentType(MediaType.APPLICATION_JSON).with(
                jwt().jwt { it.tokenValue("token").header("alg", "none").subject("1") })
                .content(objectMapper.writeValueAsString(newInformationsDto))
        ).andExpect(status().isConflict())
    }
}
