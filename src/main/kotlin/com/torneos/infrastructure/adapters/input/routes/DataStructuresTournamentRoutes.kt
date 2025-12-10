package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.tournaments.GetWaitingQueueUseCase
import com.torneos.application.usecases.tournaments.ValidateUniquePlayersUseCase
import com.torneos.application.usecases.tournaments.WithdrawFromTournamentUseCase
import com.torneos.application.usecases.standings.GetCachedStandingsUseCase
import com.torneos.domain.services.StandingsCache
import com.torneos.domain.services.TournamentWaitingQueueService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

/**
 * Rutas extendidas para demostración de estructuras de datos:
 * - Cola: Lista de espera de equipos
 * - Conjunto: Validación de jugadores únicos
 * - Diccionario: Caché de standings
 */
fun Route.dataStructuresTournamentRoutes() {
    val getWaitingQueueUseCase by application.inject<GetWaitingQueueUseCase>()
    val validateUniquePlayersUseCase by application.inject<ValidateUniquePlayersUseCase>()
    val withdrawFromTournamentUseCase by application.inject<WithdrawFromTournamentUseCase>()
    val getCachedStandingsUseCase by application.inject<GetCachedStandingsUseCase>()

    route("/tournaments") {

        // ================================================
        // COLA (Queue): Lista de espera de equipos
        // ================================================
        
        // GET /tournaments/{tournamentId}/waiting-queue
        get("/{tournamentId}/waiting-queue") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))

            try {
                val queueInfo = getWaitingQueueUseCase.execute(tournamentId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "tournamentId" to queueInfo.tournamentId.toString(),
                    "queueSize" to queueInfo.queueSize,
                    "entries" to queueInfo.entries.map { entry ->
                        mapOf(
                            "teamId" to entry.teamId.toString(),
                            "userId" to entry.userId.toString(),
                            "position" to entry.position,
                            "waitingTimeMinutes" to entry.waitingTimeMinutes
                        )
                    },
                    "structureUsed" to "Queue (Cola FIFO con ArrayDeque)"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /tournaments/{tournamentId}/waiting-queue/next
        get("/{tournamentId}/waiting-queue/next") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))

            try {
                val nextEntry = getWaitingQueueUseCase.getNext(tournamentId)
                if (nextEntry != null) {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "teamId" to nextEntry.teamId.toString(),
                        "userId" to nextEntry.userId.toString(),
                        "enqueuedAt" to nextEntry.enqueuedAt,
                        "structureUsed" to "Queue Peek"
                    ))
                } else {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "No hay equipos en la cola de espera"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /tournaments/{tournamentId}/waiting-queue/team/{teamId}/position
        get("/{tournamentId}/waiting-queue/team/{teamId}/position") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))
            
            val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))

            try {
                val position = getWaitingQueueUseCase.getTeamPosition(tournamentId, teamId)
                if (position > 0) {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "teamId" to teamId.toString(),
                        "position" to position,
                        "structureUsed" to "Queue"
                    ))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf(
                        "message" to "El equipo no está en la cola de espera"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        authenticate("auth-jwt") {
            // POST /tournaments/{tournamentId}/withdraw/{teamId}
            post("/{tournamentId}/withdraw/{teamId}") {
                val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))
                
                val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))

                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                try {
                    val result = withdrawFromTournamentUseCase.execute(tournamentId, teamId, userId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to result.success,
                        "message" to result.message,
                        "nextTeamProcessed" to result.nextTeamProcessed,
                        "nextTeamId" to result.nextTeamId?.toString(),
                        "structureUsed" to "Queue Dequeue (FIFO)"
                    ))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }

        // ================================================
        // CONJUNTO (Set): Validación de jugadores únicos
        // ================================================
        
        // GET /tournaments/{tournamentId}/validate-players
        get("/{tournamentId}/validate-players") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))

            try {
                val result = validateUniquePlayersUseCase.execute(tournamentId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "isValid" to result.isValid,
                    "message" to result.message,
                    "totalPlayersChecked" to result.totalPlayersChecked,
                    "totalTeamsChecked" to result.totalTeamsChecked,
                    "duplicatedPlayers" to result.duplicatedPlayers.map { player ->
                        mapOf(
                            "playerId" to player.playerId.toString(),
                            "playerName" to player.playerName,
                            "teamsCount" to player.teamsFound.size,
                            "teamsFound" to player.teamsFound.map { it.toString() }
                        )
                    },
                    "structureUsed" to "Set (Conjunto HashSet para detección de duplicados en O(n))"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /tournaments/{tournamentId}/player-stats
        get("/{tournamentId}/player-stats") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))

            try {
                val stats = validateUniquePlayersUseCase.getPlayerStats(tournamentId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "stats" to stats,
                    "structureUsed" to "Set (Conjunto)"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // ================================================
        // DICCIONARIO (Map): Caché de standings
        // ================================================
        
        // GET /tournaments/groups/{groupId}/standings/cached
        get("/groups/{groupId}/standings/cached") {
            val groupId = call.parameters["groupId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))

            try {
                val result = getCachedStandingsUseCase.execute(groupId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "standings" to result.standings.map { standing ->
                        mapOf(
                            "teamId" to standing.teamId.toString(),
                            "position" to standing.position,
                            "played" to standing.played,
                            "won" to standing.won,
                            "drawn" to standing.drawn,
                            "lost" to standing.lost,
                            "points" to standing.points,
                            "goalDifference" to standing.goalDifference
                        )
                    },
                    "fromCache" to result.fromCache,
                    "cacheAgeSeconds" to result.cacheAgeSeconds,
                    "structureUsed" to "Map (Diccionario ConcurrentHashMap para caché)"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        authenticate("auth-jwt") {
            // POST /tournaments/groups/{groupId}/standings/refresh
            post("/groups/{groupId}/standings/refresh") {
                val groupId = call.parameters["groupId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))

                try {
                    val result = getCachedStandingsUseCase.executeWithRefresh(groupId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "standings" to result.standings,
                        "message" to "Caché actualizado",
                        "structureUsed" to "Map (Diccionario)"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // DELETE /tournaments/cache
            delete("/cache") {
                try {
                    StandingsCache.clear()
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "Caché limpiado exitosamente",
                        "structureUsed" to "Map (Diccionario)"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // GET /tournaments/cache/stats
            get("/cache/stats") {
                try {
                    val stats = StandingsCache.getStats()
                    call.respond(HttpStatusCode.OK, mapOf(
                        "stats" to stats,
                        "structureUsed" to "Map (Diccionario)"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}
