package com.torneos.infrastructure.configuration

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import com.fasterxml.jackson.databind.SerializationFeature

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            // Desactivar serialización de fechas como timestamps
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            // Formatear JSON con indentación (opcional, mejor para debug)
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}
