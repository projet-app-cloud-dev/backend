package fr.pokecloud.auth.dto

data class NewAccountInformations(
    val username: String? = null,
    val password: NewPassword? = null,
)