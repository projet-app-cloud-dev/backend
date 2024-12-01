package fr.pokecloud.auth.service

interface PasswordService {
    fun encodePassword(password: String): String
    fun checkPassword(password: String, encryptedPassword: String): Boolean
}