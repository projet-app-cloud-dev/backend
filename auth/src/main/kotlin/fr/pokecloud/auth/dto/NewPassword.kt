package fr.pokecloud.auth.dto

data class NewPassword(
    val oldPassword: String, val newPassword: String
)