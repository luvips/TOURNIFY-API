package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.matches.GetBracketTreeUseCase
import com.torneos.application.usecases.matches.UndoMatchResultUseCase
import com.torneos.application.usecases.matches.UpdateMatchResultWithSetsUseCase
import com.torneos.domain.enums.MatchStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

/**
 * Rutas extendidas para demostración de estructuras de datos:
 * - Árbol: Visualización de bracket
 * - Pila: Deshacer resultado
 * - Arrays: Actualizar con sets
 */
fun Route.dataStructuresMatchRoutes() {
    val getBracketTreeUseCase by application.inject<GetBracketTreeUseCase>()
    val undoMatchResultUseCase by application.inject<UndoMatchResultUseCase>()
    val updateMatchResultWithSetsUseCase by application.inject<UpdateMatchResultWithSetsUseCase>()

    route("/matches") {

        // ============================================
        // ÁRBOL (Tree): Visualizar bracket como árbol
        // ============================================
        
        // GET /matches/bracket/{tournamentId}/tree
        get("/bracket/{tournamentId}/tree") {
            val tournamentId = call.parameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))

            try {
                val result = getBracketTreeUseCase.execute(tournamentId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "tree" to result.tree,
                    "depth" to result.depth,
                    "totalMatches" to result.totalMatches,
                    "structureUsed" to "Binary Tree (Árbol Binario)"
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /matches/{matchId}/path-to-final
        get("/{matchId}/path-to-final") {
            val matchId = call.parameters["matchId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de partido inválido"))

            val tournamentId = call.request.queryParameters["tournamentId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "tournamentId requerido"))

            try {
                val path = getBracketTreeUseCase.getPathToFinal(tournamentId, matchId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "path" to path.map { mapOf(
                        "id" to it.id.toString(),
                        "roundName" to it.roundName,
                        "roundNumber" to it.roundNumber
                    )},
                    "structureUsed" to "Binary Tree Traversal"
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        authenticate("auth-jwt") {

            // ================================================
            // PILA (Stack): Deshacer último resultado cargado
            // ================================================
            
            // POST /matches/{matchId}/undo
            post("/{matchId}/undo") {
                val matchId = call.parameters["matchId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de partido inválido"))

                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                try {
                    val result = undoMatchResultUseCase.execute(matchId, userId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to result.success,
                        "message" to result.message,
                        "previousState" to result.previousState,
                        "structureUsed" to "Stack (Pila LIFO)"
                    ))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // GET /matches/{matchId}/history
            get("/{matchId}/history") {
                val matchId = call.parameters["matchId"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de partido inválido"))

                try {
                    val history = undoMatchResultUseCase.getMatchHistory(matchId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "history" to history,
                        "canUndo" to undoMatchResultUseCase.canUndo(matchId),
                        "structureUsed" to "Stack (Pila)"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // ================================================
            // ARRAYS: Actualizar resultado con sets
            // ================================================
            
            // POST /matches/{matchId}/result-with-sets
            post("/{matchId}/result-with-sets") {
                val matchId = call.parameters["matchId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de partido inválido"))

                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                data class SetScoreRequest(val homeScore: Int, val awayScore: Int)
                data class UpdateWithSetsRequest(
                    val sets: List<SetScoreRequest>,
                    val winnerId: String?
                )

                try {
                    val request = call.receive<UpdateWithSetsRequest>()
                    
                    val sets = request.sets.map { 
                        UpdateMatchResultWithSetsUseCase.SetScore(it.homeScore, it.awayScore) 
                    }

                    val winnerId = request.winnerId?.let { UUID.fromString(it) }

                    val match = updateMatchResultWithSetsUseCase.execute(
                        matchId = matchId,
                        userId = userId,
                        sets = sets,
                        winnerId = winnerId
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "id" to match.id.toString(),
                        "scoreHome" to match.scoreHome,
                        "scoreAway" to match.scoreAway,
                        "homeSets" to match.homeSets,
                        "awaySets" to match.awaySets,
                        "status" to match.status.toString(),
                        "winnerId" to match.winnerId?.toString(),
                        "structureUsed" to "Arrays (Arreglos para sets)"
                    ))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // POST /matches/{matchId}/add-set
            post("/{matchId}/add-set") {
                val matchId = call.parameters["matchId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de partido inválido"))

                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                data class AddSetRequest(val homeScore: Int, val awayScore: Int)

                try {
                    val request = call.receive<AddSetRequest>()
                    
                    val match = updateMatchResultWithSetsUseCase.addSet(
                        matchId = matchId,
                        userId = userId,
                        homeScore = request.homeScore,
                        awayScore = request.awayScore
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "id" to match.id.toString(),
                        "homeSets" to match.homeSets,
                        "awaySets" to match.awaySets,
                        "currentScore" to "${match.scoreHome}-${match.scoreAway}",
                        "structureUsed" to "Arrays (Arreglos dinámicos)"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}
