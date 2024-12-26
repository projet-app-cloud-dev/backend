package fr.pokecloud.auth.service

import fr.pokecloud.auth.database.UserRepository
import fr.pokecloud.auth.model.User
import fr.pokecloud.auth.model.exception.UserNotFoundException
import fr.pokecloud.auth.model.exception.UsernameTakenException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun getUserById(userId: Long): User? = userRepository.findUserById(userId)?.let {
        User(it.id!!, it.username, it.encodedPassword)
    }

    fun getUserByUsername(username: String): User? = userRepository.findUserByUsername(username)?.let {
        User(it.id!!, it.username, it.encodedPassword)
    }

    @Throws(UserNotFoundException::class, UsernameTakenException::class)
    fun editUser(userId: Long, username: String?, password: String?) {
        try {
            val newUser = userRepository.findUserById(userId)?.also { user ->
                if (!username.isNullOrBlank()) {
                    user.username = username
                }
                if (!password.isNullOrBlank()) {
                    user.encodedPassword = passwordEncoder.encode(password)
                }
            } ?: throw UserNotFoundException()
            userRepository.save(newUser)
        } catch (e: DataIntegrityViolationException) {
            throw UsernameTakenException()
        }
    }

    @Throws(UsernameTakenException::class)
    fun createUser(username: String, password: String): User {
        return try {
            userRepository.save(
                fr.pokecloud.auth.database.entities.User(
                    null, username, passwordEncoder.encode(password)
                )
            ).let {
                User(it.id!!, it.username, it.encodedPassword)
            }
        } catch (e: DataIntegrityViolationException) {
            throw UsernameTakenException()
        }
    }
}