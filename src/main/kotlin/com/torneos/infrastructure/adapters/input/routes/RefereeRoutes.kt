package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.matches.GetRefereeMatchesUseCase
import com.torneos.infrastructure.adapters.input.mappers.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.refereeRoutes() {
    val getRefereeMatchesUseCase by application.inject<GetRefereeMatchesUseCase>()

    route("/referees") {
        
        authenticate("auth-jwt") {
            
            // Obtener partidos asignados al árbitro autenticado
            get("/my-matches") {
                val userId = UUID.fromString(
                    call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                )
                
                try {
                    val matches = getRefereeMatchesUseCase.execute(userId)
                    call.respond(HttpStatusCode.OK, matches.map { it.toResponse() })
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Error al obtener partidos"))
                    )
                }
            }
        }
    }
}
