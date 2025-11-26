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
    val deleteTournamentUseCase by application.inject<DeleteTournamentUseCase>()
    val updateTournamentUseCase by application.inject<UpdateTournamentUseCase>()

    route("/tournaments") {

        // RUTAS PÚBLICAS
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

        // RUTAS PROTEGIDAS
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("id")?.asString()
                val userRole = principal?.payload?.getClaim("role")?.asString()

                if (userIdStr == null) return@post call.respond(HttpStatusCode.Unauthorized)
                if (userRole !in listOf("organizer", "admin")) return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Acceso denegado."))

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val created = createTournamentUseCase.execute(domainTournament)
                    call.respond(HttpStatusCode.Created, created.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                val tournamentIdStr = call.parameters["id"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                if (tournamentIdStr == null || userIdStr == null) return@put call.respond(HttpStatusCode.BadRequest)

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val updated = updateTournamentUseCase.execute(UUID.fromString(tournamentIdStr), domainTournament, UUID.fromString(userIdStr))
                    call.respond(HttpStatusCode.OK, updated.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString() ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                try {
                    deleteTournamentUseCase.execute(UUID.fromString(id), UUID.fromString(userId))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Torneo eliminado"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/{id}/join") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                if (userIdStr == null || tournamentIdStr == null) return@post call.respond(HttpStatusCode.BadRequest)

                try {
                    val request = call.receive<JoinTournamentRequest>()
                    joinTournamentUseCase.execute(UUID.fromString(userIdStr), UUID.fromString(tournamentIdStr), UUID.fromString(request.teamId))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Inscripción enviada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                }
            }

            post("/{id}/follow") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                
                if (userIdStr == null || tournamentIdStr == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Token o ID inválido"))
                }

                try {
                    followTournamentUseCase.execute(
                        userId = UUID.fromString(userIdStr), 
                        tournamentId = UUID.fromString(tournamentIdStr)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Ahora sigues este torneo"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Ya seguías este torneo o hubo un error."))
                }
            }

            delete("/{id}/follow") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                
                if (userIdStr == null || tournamentIdStr == null) {
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Token o ID inválido"))
                }

                try {
                    unfollowTournamentUseCase.execute(
                        userId = UUID.fromString(userIdStr), 
                        tournamentId = UUID.fromString(tournamentIdStr)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Dejaste de seguir este torneo"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al dejar de seguir.", "details" to e.message))
                }
            }
        }
    }
}