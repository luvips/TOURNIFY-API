package com.torneos.infrastructure.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.server.plugins.contentnegotiation.*
import com.fasterxml.jackson.databind.SerializationFeature

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {

            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            enable(SerializationFeature.INDENT_OUTPUT)
        }

    }
}
