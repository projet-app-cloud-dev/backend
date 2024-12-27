package fr.pokecloud.collection.model.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class CardApiException : Exception {
    constructor(t: Throwable) : super("Card API request exception", t)
    constructor(msg: String) : super(msg)
}