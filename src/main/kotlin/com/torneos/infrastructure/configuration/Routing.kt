package com.torneos.infrastructure.configuration

import com.torneos.infrastructure.adapters.input.routes.authRoutes
import com.torneos.infrastructure.adapters.input.routes.tournamentRoutes
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Registramos los módulos de rutas
        authRoutes()
        tournamentRoutes()

        // Health check
        get("/") {
            call.respondText("Tournify API corriendo")
        }
    }
}