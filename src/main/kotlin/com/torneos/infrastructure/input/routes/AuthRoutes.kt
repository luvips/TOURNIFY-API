package com.torneos.infrastructure.adapters.input.routes

import com.torneos.infrastructure.adapters.input.dtos.LoginRequest
import com.torneos.infrastructure.adapters.input.dtos.RegisterRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            // TODO: Llamar a RegisterUseCase.execute(request)
            call.respond(HttpStatusCode.Created, mapOf("message" to "Usuario registrado (Simulado)"))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            // TODO: Llamar a LoginUseCase.execute(request)
            // Respuesta simulada compatible con tu Frontend
            call.respond(HttpStatusCode.OK, mapOf(
                "token" to "fake-jwt-token",
                "userId" to "user-123",
                "username" to "Test User",
                "role" to "organizer"
            ))
        }
    }
}