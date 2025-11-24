package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.teams.CreateTeamUseCase
import com.torneos.application.usecases.teams.AddMemberUseCase
import com.torneos.application.usecases.teams.GetMyTeamsUseCase
import com.torneos.infrastructure.adapters.input.dtos.AddMemberRequest
import com.torneos.infrastructure.adapters.input.dtos.CreateTeamRequest
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

fun Route.teamRoutes() {
    val createTeamUseCase by inject<CreateTeamUseCase>()
    val getMyTeamsUseCase by inject<GetMyTeamsUseCase>()
    val addMemberUseCase by inject<AddMemberUseCase>()

    route("/teams") {
        authenticate("auth-jwt") {

            // Mis equipos
            get("/my") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                val teams = getMyTeamsUseCase.execute(userId)
                call.respond(HttpStatusCode.OK, teams.map { it.toResponse() })
            }

            // Crear equipo
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                val request = call.receive<CreateTeamRequest>()
                val team = createTeamUseCase.execute(request.toDomain(userId))

                call.respond(HttpStatusCode.Created, team.toResponse())
            }

            // Agregar miembro
            post("/{id}/members") {
                val teamId = UUID.fromString(call.parameters["id"])
                val request = call.receive<AddMemberRequest>()

                // TODO: Validar que el usuario actual es el capitán del equipo
                val member = addMemberUseCase.execute(request.toDomain(teamId))
                call.respond(HttpStatusCode.OK, mapOf("message" to "Miembro agregado"))
            }
        }
    }
}