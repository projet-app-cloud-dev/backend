package fr.pokecloud.auth.service

import fr.pokecloud.auth.database.UserRepository
import fr.pokecloud.auth.model.exception.UserNotFoundException
import fr.pokecloud.auth.model.exception.UsernameTakenException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.*
import fr.pokecloud.auth.database.entities.User as UserEntity

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `test getUserById return connect user`() {
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        val user = userService.getUserById(dbUser.id!!)
        assertNotNull(user)
        assertEquals(dbUser.username, user.username)
        assertEquals(dbUser.encodedPassword, user.encodedPassword)
    }

    @Test
    fun `test get userById not found return null`() {
        val user = userService.getUserById(0)
        assertNull(user)
    }

    @Test
    fun `test getUserByUsername return connect user`() {
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        userRepository.save(UserEntity(null, "test2", "bar"))
        val user = userService.getUserByUsername(dbUser.username)
        assertNotNull(user)
        assertEquals(dbUser.id, user.userId)
        assertEquals(dbUser.username, user.username)
        assertEquals(dbUser.encodedPassword, user.encodedPassword)
    }

    @Test
    fun `test getUserByUsername not found return null`() {
        val user = userService.getUserByUsername("test")
        assertNull(user)
    }

    @Test
    fun `test edit user with username and password is ok`() {
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        userService.editUser(dbUser.id!!, "bar", "foo")
        val user = userService.getUserById(dbUser.id!!)
        assertNotNull(user)
        assertEquals(dbUser.id, user.userId)
        assertEquals("bar", user.username)
        assertTrue(passwordEncoder.matches("foo", user.encodedPassword))
    }


    @Test
    fun `test edit user with username is ok`() {
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        userService.editUser(dbUser.id!!, "bar", null)
        val user = userService.getUserById(dbUser.id!!)
        assertNotNull(user)
        assertEquals(dbUser.id, user.userId)
        assertEquals("bar", user.username)
        assertEquals("foo", user.encodedPassword)
    }

    @Test
    fun `test edit user with password is ok`() {
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        userService.editUser(dbUser.id!!, null, "bar")
        val user = userService.getUserById(dbUser.id!!)
        assertNotNull(user)
        assertEquals(dbUser.id, user.userId)
        assertEquals("test", user.username)
        assertTrue(passwordEncoder.matches("bar", user.encodedPassword))
    }

    @Test
    fun `test edit user with existing username throws exception`() {
        val existingUser = userRepository.save(UserEntity(null, "other", "foo"))
        val dbUser = userRepository.save(UserEntity(null, "test", "foo"))
        assertThrows<UsernameTakenException> { userService.editUser(dbUser.id!!, existingUser.username, "bar") }
        val user = userService.getUserById(dbUser.id!!)
        assertNotNull(user)
        assertEquals(dbUser.id, user.userId)
        assertEquals("test", user.username)
        assertEquals("foo", user.encodedPassword)
    }

    @Test
    fun `test edit user with invalid id throws exception`() {
        assertThrows<UserNotFoundException> { userService.editUser(1L, "foo", "bar") }
    }

    @Test
    fun `test create user return user`() {
        val newUser = userService.createUser("test", "foo")
        val dbUser = userRepository.findUserById(newUser.userId)
        assertNotNull(dbUser)
        assertEquals(newUser.username, dbUser.username)
        assertEquals(newUser.encodedPassword, dbUser.encodedPassword)
        assertTrue(passwordEncoder.matches("foo", dbUser.encodedPassword))
    }

    @Test
    fun `test create user with existing username throws exception`() {
        userRepository.save(UserEntity(null, "test", "foo"))
        assertThrows<UsernameTakenException> { userService.createUser("test", "foo") }
    }
}