package fr.pokecloud.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(private val passwordEncoder: PasswordEncoder) {
    fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
    }

    fun checkPassword(password: String, encryptedPassword: String): Boolean {
        return passwordEncoder.matches(password, encryptedPassword)
    }
}