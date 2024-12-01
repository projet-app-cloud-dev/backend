package fr.pokecloud.auth.model

data class User(val userId: Long, val username: String, val encodedPassword: String)
