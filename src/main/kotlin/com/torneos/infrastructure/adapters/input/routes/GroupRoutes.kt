package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.groups.*
import com.torneos.application.usecases.tournaments.GetTournamentStandingsUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.groupRoutes() {
    val generateGroupsUseCase by application.inject<GenerateGroupsUseCase>()
    val assignTeamsUseCase by application.inject<AssignTeamsToGroupsUseCase>()
    val generateGroupMatchesUseCase by application.inject<GenerateGroupMatchesUseCase>()
    val getStandingsUseCase by application.inject<GetTournamentStandingsUseCase>()

    route("/tournaments/{id}/groups") {

        // Ver Tabla de Posiciones (Público)
        get("/standings") {
            val id = UUID.fromString(call.parameters["id"])
            val standings = getStandingsUseCase.execute(id)
            call.respond(HttpStatusCode.OK, standings)
        }

        authenticate("auth-jwt") {
            // 1. Crear Grupos
            post("/generate") {
                val tournamentId = UUID.fromString(call.parameters["id"])
                val userId = UUID.fromString(call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString())

                try {
                    val groups = generateGroupsUseCase.execute(tournamentId, userId)
                    call.respond(HttpStatusCode.Created, groups)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // 2. Asignar Equipos y Generar Calendario
            post("/initialize") {
                val tournamentId = UUID.fromString(call.parameters["id"])
                try {
                    //  Repartir equipos
                    assignTeamsUseCase.execute(tournamentId)
                    //  Crear partidos
                    generateGroupMatchesUseCase.execute(tournamentId)

                    call.respond(HttpStatusCode.OK, mapOf("message" to "Fase de grupos iniciada correctamente"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
}