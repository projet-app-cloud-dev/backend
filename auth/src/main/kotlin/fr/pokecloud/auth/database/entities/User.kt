package fr.pokecloud.auth.database.entities

import jakarta.persistence.*

@Entity(name = "APP_USER")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,

    @Column(unique = true) val username: String,

    @Column val encodedPassword: String,
)