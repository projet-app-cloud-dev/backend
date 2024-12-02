package fr.pokecloud.auth.database

import fr.pokecloud.auth.database.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
    fun findUserById(id: Long): User?
}