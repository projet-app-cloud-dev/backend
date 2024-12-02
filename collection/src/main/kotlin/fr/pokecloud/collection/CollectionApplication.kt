package fr.pokecloud.collection

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    name = "auth_token",
    scheme = "Bearer",
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
class CollectionApplication

fun main(args: Array<String>) {
    runApplication<CollectionApplication>(*args)
}
