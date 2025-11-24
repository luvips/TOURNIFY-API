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
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.tournamentRoutes() {
    // --- INYECCIÓN DE DEPENDENCIAS ---
    // Básicos
    val createTournamentUseCase by inject<CreateTournamentUseCase>()
    val getTournamentsUseCase by inject<GetTournamentsUseCase>()
    val getTournamentDetailsUseCase by inject<GetTournamentDetailsUseCase>()

    // Funcionalidades Avanzadas (Asegúrate de tener estos UseCases creados)
    val getTournamentStandingsUseCase by inject<GetTournamentStandingsUseCase>()
    val getTournamentMatchesUseCase by inject<GetTournamentMatchesUseCase>() // Para el Bracket
    val getTournamentTeamsUseCase by inject<GetTournamentTeamsUseCase>()     // Para ver participantes
    val joinTournamentUseCase by inject<JoinTournamentUseCase>()             // Inscripción
    val followTournamentUseCase by inject<FollowTournamentUseCase>()
    val unfollowTournamentUseCase by inject<UnfollowTournamentUseCase>()

    route("/tournaments") {

        // ==========================================
        //              ENDPOINTS PÚBLICOS
        // ==========================================

        // 1. Listar torneos (Feed)
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val tournaments = getTournamentsUseCase.execute(page, size).map { it.toResponse() }
                call.respond(HttpStatusCode.OK, tournaments)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error desconocido")))
            }
        }

        // 2. Detalle del Torneo
        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val tournament = getTournamentDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, tournament.toResponse())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Torneo no encontrado"))
            }
        }

        // 3. Tabla de Posiciones (Standings)
        get("/{id}/standings") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val standings = getTournamentStandingsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, standings)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.OK, emptyList<Any>()) // Retornar vacío si no hay tabla aún
            }
        }

        // 4. Partidos / Bracket (NECESARIO PARA EL FRONTEND DE LLAVES)
        get("/{id}/matches") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                // Debe retornar la lista de partidos con su 'roundNumber', 'matchNumber', 'nextMatchId'
                // para que el frontend pueda dibujar las líneas del bracket.
                val matches = getTournamentMatchesUseCase.execute(UUID.fromString(idParam))
                // Mapear matches a DTOs de respuesta (MatchResponse)
                call.respond(HttpStatusCode.OK, matches)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al cargar el bracket"))
            }
        }

        // 5. Equipos Participantes
        get("/{id}/teams") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val teams = getTournamentTeamsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, teams) // Retorna TeamResponse list
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al cargar equipos"))
            }
        }

        // ==========================================
        //            ENDPOINTS PROTEGIDOS
        // ==========================================
        authenticate("auth-jwt") {

            // 6. Crear Torneo
            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                if (userId == null) { call.respond(HttpStatusCode.Unauthorized); return@post }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userId))
                    val created = createTournamentUseCase.execute(domainTournament)
                    call.respond(HttpStatusCode.Created, created.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error al crear")))
                }
            }

            // 7. Inscribir un Equipo (JOIN)
            post("/{id}/join") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                if (userId == null || tournamentIdStr == null) { call.respond(HttpStatusCode.BadRequest); return@post }

                try {
                    // El usuario envía con qué equipo se quiere inscribir
                    val request = call.receive<JoinTournamentRequest>() // { teamId: "..." }

                    joinTournamentUseCase.execute(
                        userId = UUID.fromString(userId), // Validar que es capitán
                        tournamentId = UUID.fromString(tournamentIdStr),
                        teamId = UUID.fromString(request.teamId)
                    )

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Inscripción enviada con éxito"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "No se pudo inscribir")))
                }
            }

            // 8. Seguir Torneo (Follow)
            post("/{id}/follow") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentId = call.parameters["id"]
                if (userId != null && tournamentId != null) {
                    followTournamentUseCase.execute(UUID.fromString(userId), UUID.fromString(tournamentId))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Siguiendo"))
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            // 9. Dejar de Seguir (Unfollow)
            delete("/{id}/follow") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentId = call.parameters["id"]
                if (userId != null && tournamentId != null) {
                    unfollowTournamentUseCase.execute(UUID.fromString(userId), UUID.fromString(tournamentId))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Dejado de seguir"))
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}