package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.matches.GetMatchDetailsUseCase
import com.torneos.application.usecases.matches.UpdateMatchResultUseCase
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
import java.util.UUID

fun Route.matchRoutes() {
    // Inyección de Casos de Uso
    val updateMatchResultUseCase by application.inject<UpdateMatchResultUseCase>()
    val getMatchDetailsUseCase by application.inject<GetMatchDetailsUseCase>()

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

        // 2. Actualizar Resultado (Protegido: Árbitro/Admin)
        authenticate("auth-jwt") {
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
        }
    }
}