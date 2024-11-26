package fr.pokecloud.cards

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CardsApplication

fun main(args: Array<String>) {
    runApplication<CardsApplication>(*args)
}
