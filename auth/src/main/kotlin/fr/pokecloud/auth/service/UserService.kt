package fr.pokecloud.auth.service

import fr.pokecloud.auth.model.User
import fr.pokecloud.auth.model.exception.UsernameTakenException


interface UserService {
    fun getUserById(userId: Long): User?
    fun getUserByUsername(username: String): User?

    @Throws(UsernameTakenException::class)
    fun editUser(userId: Long, username: String?, password: String?)

    @Throws(UsernameTakenException::class)
    fun createUser(username: String, password: String): User
}