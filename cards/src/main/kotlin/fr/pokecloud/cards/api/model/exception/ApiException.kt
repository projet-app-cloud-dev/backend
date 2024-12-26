package fr.pokecloud.cards.api.model.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ApiException : Exception {
    constructor(message: String) : super(message)
    constructor(t: Throwable) : super("API Error", t.cause)
}