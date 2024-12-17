package fr.pokecloud.collection.model.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CardNotFoundException : Exception()

@ResponseStatus(HttpStatus.NOT_FOUND)
class CollectionNotFoundException : Exception()