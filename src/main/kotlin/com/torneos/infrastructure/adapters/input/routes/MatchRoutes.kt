package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.matches.CreateMatchUseCase
import com.torneos.application.usecases.matches.DeleteMatchUseCase
import com.torneos.application.usecases.matches.GenerateBracketUseCase
import com.torneos.application.usecases.matches.GetMatchDetailsUseCase
import com.torneos.application.usecases.matches.UpdateMatchResultUseCase
import com.torneos.infrastructure.adapters.input.dtos.CreateMatchRequest
import com.torneos.infrastructure.adapters.input.dtos.GenerateBracketRequest
import com.torneos.infrastructure.adapters.input.dtos.UpdateMatchResultRequest
import com.torneos.infrastructure.adapters.input.mappers.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.Instant
import java.util.UUID

fun Route.matchRoutes() {
    // Inyección de Casos de Uso
    val createMatchUseCase by application.inject<CreateMatchUseCase>()
    val generateBracketUseCase by application.inject<GenerateBracketUseCase>()
    val updateMatchResultUseCase by application.inject<UpdateMatchResultUseCase>()
    val getMatchDetailsUseCase by application.inject<GetMatchDetailsUseCase>()
    val deleteMatchUseCase by application.inject<DeleteMatchUseCase>()
    
    route("/matches") {

        // 1. Ver detalle de un partido (Público)
        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val match = getMatchDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, match.toResponse())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Partido no encontrado"))
            }
        }

        authenticate("auth-jwt") {
            
            // 2. Crear Partido Manual (Protegido: Organizador)
            post {
                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())
                
                try {
                    val request = call.receive<CreateMatchRequest>()
                    
                    // Parsear fecha si se proporciona
                    val scheduledDate = request.scheduledDate?.let { Instant.parse(it) }
                    
                    // El tournamentId debe venir en el request o extraerse del contexto
                    // Para simplificar, asumimos que viene en el body
                    // En producción, podría ser POST /tournaments/{id}/matches
                    
                    val match = createMatchUseCase.execute(
                        userId = userId,
                        tournamentId = UUID.fromString(request.groupId ?: throw IllegalArgumentException("tournamentId requerido")), // TEMPORAL
                        teamHomeId = request.teamHomeId?.let { UUID.fromString(it) },
                        teamAwayId = request.teamAwayId?.let { UUID.fromString(it) },
                        scheduledDate = scheduledDate,
                        location = request.location,
                        roundName = request.roundName,
                        roundNumber = request.roundNumber,
                        matchNumber = request.matchNumber,
                        groupId = request.groupId?.let { UUID.fromString(it) },
                        refereeId = request.refereeId?.let { UUID.fromString(it) }
                    )
                    
                    call.respond(HttpStatusCode.Created, match.toResponse())
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al crear partido")))
                }
            }
            
            // 3. Actualizar Resultado (Protegido: Árbitro/Admin)
            put("/{id}/result") {
                val matchIdParam = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                try {
                    val request = call.receive<UpdateMatchResultRequest>()

                    // Ejecutar lógica de negocio (validar que el usuario sea el referee o admin)
                    val updatedMatch = updateMatchResultUseCase.execute(
                        matchId = UUID.fromString(matchIdParam),
                        userId = userId, // Para validar permisos
                        scoreHome = request.scoreHome,
                        scoreAway = request.scoreAway,
                        status = request.status,
                        winnerId = request.winnerId?.let { UUID.fromString(it) }
                    )

                    call.respond(HttpStatusCode.OK, updatedMatch.toResponse())
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No tienes permiso para arbitrar este partido"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error desconocido")))
                }
            }
            
            // 4. Eliminar Partido (Protegido: Organizador/Admin)
            delete("/{id}") {
                val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                try {
                    deleteMatchUseCase.execute(UUID.fromString(idParam))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Partido eliminado correctamente"))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Partido no encontrado"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar partido"))
                }
            }
        }
    }
}

/**
 * Rutas específicas de torneos para matches
 * POST /tournaments/{id}/generate-bracket - Generar bracket automático
 * POST /tournaments/{id}/matches - Crear partido en un torneo
 */
fun Route.tournamentMatchRoutes() {
    val generateBracketUseCase by application.inject<GenerateBracketUseCase>()
    val createMatchUseCase by application.inject<CreateMatchUseCase>()
    val teamRepository by application.inject<com.torneos.domain.ports.TeamRepository>()
    
    route("/tournaments/{tournamentId}") {
        authenticate("auth-jwt") {
            
            // Generar Bracket Automático
            post("/generate-bracket") {
                val tournamentIdParam = call.parameters["tournamentId"] 
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "tournamentId requerido"))
                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())
                
                try {
                    val request = call.receiveNullable<GenerateBracketRequest>()
                    val startDate = request?.startDate?.let { Instant.parse(it) }
                    
                    val matches = generateBracketUseCase.execute(
                        userId = userId,
                        tournamentId = UUID.fromString(tournamentIdParam),
                        startDate = startDate
                    )
                    
                    call.respond(HttpStatusCode.Created, mapOf(
                        "message" to "Bracket generado exitosamente",
                        "matchesCreated" to matches.size,
                        "matches" to matches.map { it.toResponse() }
                    ))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al generar bracket")))
                }
            }
            
            // Crear Partido en Torneo
            post("/matches") {
                val tournamentIdParam = call.parameters["tournamentId"] 
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "tournamentId requerido"))
                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())
                
                try {
                    val request = call.receive<CreateMatchRequest>()
                    val scheduledDate = request.scheduledDate?.let { Instant.parse(it) }
                    
                    val match = createMatchUseCase.execute(
                        userId = userId,
                        tournamentId = UUID.fromString(tournamentIdParam),
                        teamHomeId = request.teamHomeId?.let { UUID.fromString(it) },
                        teamAwayId = request.teamAwayId?.let { UUID.fromString(it) },
                        scheduledDate = scheduledDate,
                        location = request.location,
                        roundName = request.roundName,
                        roundNumber = request.roundNumber,
                        matchNumber = request.matchNumber,
                        groupId = request.groupId?.let { UUID.fromString(it) },
                        refereeId = request.refereeId?.let { UUID.fromString(it) }
                    )
                    
                    // Cargar nombres de equipos
                    val homeTeam = match.teamHomeId?.let { teamRepository.findById(it) }
                    val awayTeam = match.teamAwayId?.let { teamRepository.findById(it) }
                    
                    val response = com.torneos.infrastructure.adapters.input.dtos.MatchResponse(
                        id = match.id.toString(),
                        homeTeamName = homeTeam?.name ?: "TBD",
                        awayTeamName = awayTeam?.name ?: "TBD",
                        scoreHome = match.scoreHome,
                        scoreAway = match.scoreAway,
                        status = match.status,
                        scheduledDate = match.scheduledDate?.toString()
                    )
                    
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al crear partido")))
                }
            }
        }
    }
}