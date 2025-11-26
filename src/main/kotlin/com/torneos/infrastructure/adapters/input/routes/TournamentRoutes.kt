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

        // 1. Listar Torneos
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val tournaments = getTournamentsUseCase.execute(page, size)
                call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error desconocido")))
            }
        }

        // 2. Detalle de Torneo
        get("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Falta el ID del torneo"))
                return@get
            }

            try {
                val tournament = getTournamentDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, tournament.toResponse())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Torneo no encontrado"))
            }
        }

        authenticate("auth-jwt") {
            // 3. Crear Torneo (POST)
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("id")?.asString()
                val userRole = principal?.payload?.getClaim("role")?.asString()

                if (userIdStr == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido"))
                }

                if (userRole !in listOf("organizer", "admin")) {
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo organizadores pueden crear torneos. Tu rol es: $userRole"))
                }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val created = createTournamentUseCase.execute(domainTournament)
                    call.respond(HttpStatusCode.Created, created.toResponse())

                } catch (e: SerializationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "JSON mal formado", "details" to e.message))
                } catch (e: DateTimeParseException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Formato de fecha inválido", "details" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Datos inválidos", "details" to e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor", "details" to e.message))
                }
            }

            // 4. Actualizar Torneo (PUT)
            put("/{id}") {
                val tournamentIdStr = call.parameters["id"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

                if (tournamentIdStr == null || userIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID o Token inválido"))
                    return@put
                }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val updated = updateTournamentUseCase.execute(
                        id = UUID.fromString(tournamentIdStr),
                        tournament = domainTournament,
                        requesterId = UUID.fromString(userIdStr)
                    )
                    call.respond(HttpStatusCode.OK, updated.toResponse())
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Datos inválidos", "details" to e.message))
                }
            }

            // 5. Borrar Torneo (DELETE)
            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                try {
                    deleteTournamentUseCase.execute(UUID.fromString(id), UUID.fromString(userId))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Torneo eliminado"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error")))
                }
            }

            // 6. Unirse a Torneo
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