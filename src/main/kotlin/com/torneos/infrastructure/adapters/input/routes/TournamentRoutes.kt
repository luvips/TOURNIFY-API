package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.tournaments.*
import com.torneos.infrastructure.adapters.input.dtos.CreateTournamentRequest
import com.torneos.infrastructure.adapters.input.dtos.JoinTournamentRequest
import com.torneos.infrastructure.adapters.input.mappers.toDomain
import com.torneos.infrastructure.adapters.input.mappers.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import org.koin.ktor.ext.inject
import java.time.format.DateTimeParseException
import java.util.UUID

fun Route.tournamentRoutes() {
    val createTournamentUseCase by application.inject<CreateTournamentUseCase>()
    val getTournamentsUseCase by application.inject<GetTournamentsUseCase>()
    val getTournamentDetailsUseCase by application.inject<GetTournamentDetailsUseCase>()
    val getTournamentStandingsUseCase by application.inject<GetTournamentStandingsUseCase>()
    val getTournamentMatchesUseCase by application.inject<GetTournamentMatchesUseCase>()
    val getTournamentTeamsUseCase by application.inject<GetTournamentTeamsUseCase>()
    val joinTournamentUseCase by application.inject<JoinTournamentUseCase>()
    val followTournamentUseCase by application.inject<FollowTournamentUseCase>()
    val unfollowTournamentUseCase by application.inject<UnfollowTournamentUseCase>()

    route("/tournaments") {

        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val tournaments = getTournamentsUseCase.execute(page, size)
                call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error desconocido")))
            }
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val tournament = getTournamentDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, tournament.toResponse())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Torneo no encontrado"))
            }
        }

        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("id")?.asString()
                val userRole = principal?.payload?.getClaim("role")?.asString()

                if (userIdStr == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                if (userRole !in listOf("organizer", "admin")) {
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso denegado."))
                }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val created = createTournamentUseCase.execute(domainTournament)
                    call.respond(HttpStatusCode.Created, created.toResponse())

                } catch (e: SerializationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "JSON mal formado o tipo de dato incorrecto.",
                        "details" to e.message
                    ))
                } catch (e: DateTimeParseException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Formato de fecha inválido.",
                        "details" to "La fecha '${e.parsedString}' no es válida. Se espera formato ISO 8601 (ej: 2025-12-01T10:00:00Z)."
                    ))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Argumento inválido.",
                        "details" to "Probablemente un UUID es incorrecto o el sportId no existe. ${e.message}"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Error inesperado en el servidor.",
                        "details" to e.message
                    ))
                }
            }
            
            post("/{id}/join") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                if (userIdStr == null || tournamentIdStr == null) { 
                    call.respond(HttpStatusCode.BadRequest)
                    return@post 
                }

                try {
                    val request = call.receive<JoinTournamentRequest>()
                    joinTournamentUseCase.execute(
                        userId = UUID.fromString(userIdStr),
                        tournamentId = UUID.fromString(tournamentIdStr),
                        teamId = UUID.fromString(request.teamId)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Inscripción enviada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Error")))
                }
            }
        }
    }
}