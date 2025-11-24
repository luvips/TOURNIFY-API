package com.torneos.infrastructure.adapters.input.routes

import com.torneos.infrastructure.adapters.input.dtos.CreateTournamentRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tournamentRoutes() {
    route("/tournaments") {

        // Listar todos (Público o Protegido según decidas)
        get {
            // TODO: Llamar a GetAllTournamentsUseCase
            call.respond(HttpStatusCode.OK, listOf<String>()) // Lista vacía por ahora
        }

        // Rutas protegidas (requieren Token)
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("id")?.asString()

                val request = call.receive<CreateTournamentRequest>()

                // TODO: Llamar a CreateTournamentUseCase(request, userId)

                call.respond(HttpStatusCode.Created, mapOf("message" to "Torneo creado (Simulado)"))
            }

            // Unirse a torneo
            post("/{id}/join") {
                val tournamentId = call.parameters["id"]
                // TODO: Llamar a JoinTournamentUseCase
                call.respond(HttpStatusCode.OK, mapOf("message" to "Solicitud enviada"))
            }
        }
    }
}