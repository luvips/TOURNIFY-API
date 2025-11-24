package com.torneos.infrastructure.configuration

import com.torneos.infrastructure.adapters.input.routes.* // Importar todo
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Registrar TODAS las rutas
        authRoutes()
        userRoutes()
        tournamentRoutes()
        teamRoutes()
        matchRoutes()
        sportRoutes()

        get("/") {
            call.respondText("Tournify API corriendo (ROUTING)")
        }
    }
}