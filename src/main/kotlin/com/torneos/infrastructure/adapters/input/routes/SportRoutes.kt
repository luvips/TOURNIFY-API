package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.sports.GetSportsUseCase
import com.torneos.application.usecases.sports.CreateSportUseCase
import com.torneos.infrastructure.adapters.input.dtos.CreateSportRequest
import com.torneos.infrastructure.adapters.input.mappers.toDomain
import com.torneos.infrastructure.adapters.input.mappers.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.sportRoutes() {
    val getSportsUseCase by application.inject<GetSportsUseCase>()
    val createSportUseCase by application.inject<CreateSportUseCase>()

    route("/sports") {
        // Público: Listar deportes
        get {
            val sports = getSportsUseCase.execute()
            call.respond(HttpStatusCode.OK, sports.map { it.toResponse() })
        }

        // Admin: Crear deporte
        authenticate("auth-jwt") {
            post {
                // TODO: Verificar rol ADMIN
                val request = call.receive<CreateSportRequest>()
                val sport = createSportUseCase.execute(request.toDomain())
                call.respond(HttpStatusCode.Created, sport.toResponse())
            }
        }
    }
}