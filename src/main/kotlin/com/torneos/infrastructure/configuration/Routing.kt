package com.torneos.infrastructure.configuration

import com.torneos.infrastructure.adapters.input.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Registrar módulos de rutas
        authRoutes()
        userRoutes()
        tournamentRoutes()
        teamRoutes()
        matchRoutes()
        tournamentMatchRoutes() // Nuevas rutas para generación de bracket
        sportRoutes()
        
        // Health Check
        get("/") {
            call.respondText("Tournify Backend is Live! 🚀")
        }
    }
}