package io.github.edgeatzero.booksource.dmzj

import kotlinx.serialization.json.Json

internal val JSON = Json {
    ignoreUnknownKeys = true
    isLenient = true
}