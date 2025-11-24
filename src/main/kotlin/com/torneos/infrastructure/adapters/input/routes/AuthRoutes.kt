package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.auth.LoginUseCase
import com.torneos.application.usecases.auth.RegisterUserUseCase
import com.torneos.infrastructure.adapters.input.dtos.AuthResponse
import com.torneos.infrastructure.adapters.input.dtos.LoginRequest
import com.torneos.infrastructure.adapters.input.dtos.RegisterRequest
import com.torneos.infrastructure.adapters.input.mappers.toDomain
import com.torneos.infrastructure.adapters.input.mappers.toDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val loginUseCase by inject<LoginUseCase>()
    val registerUseCase by inject<RegisterUserUseCase>()

    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val user = registerUseCase.execute(request.toDomain(), request.password)
                call.respond(HttpStatusCode.Created, user.toDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            // LoginUseCase retorna Pair<String, User>? (Token y Usuario)
            val result = loginUseCase.execute(request.email, request.password)

            if (result != null) {
                val (token, user) = result
                call.respond(HttpStatusCode.OK, AuthResponse(token, user.toDto()))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
            }
        }
    }
}