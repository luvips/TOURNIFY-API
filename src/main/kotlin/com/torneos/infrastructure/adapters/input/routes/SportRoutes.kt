package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.sports.GetSportsUseCase
import com.torneos.application.usecases.sports.CreateSportUseCase
import com.torneos.application.usecases.sports.DeleteSportUseCase
import com.torneos.application.usecases.sports.UpdateSportUseCase
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
import java.util.UUID

fun Route.sportRoutes() {
    val getSportsUseCase by application.inject<GetSportsUseCase>()
    val createSportUseCase by application.inject<CreateSportUseCase>()
    val updateSportUseCase by application.inject<UpdateSportUseCase>()
    val deleteSportUseCase by application.inject<DeleteSportUseCase>()

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
            put("/{id}") {
                val idStr = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)

                try {
                    // Reutilizamos CreateSportRequest para simplificar, o crea un UpdateSportRequest
                    val request = call.receive<CreateSportRequest>()

                    val updatedSport = updateSportUseCase.execute(
                        UUID.fromString(idStr),
                        request.toDomain() // Convierte el JSON a modelo Sport
                    )

                    call.respond(HttpStatusCode.OK, updatedSport.toResponse())
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deporte no encontrado"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Datos inválidos"))
                }
            }

            // 👇 ELIMINAR DEPORTE
            delete("/{id}") {
                val idStr = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                try {
                    deleteSportUseCase.execute(UUID.fromString(idStr))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Deporte eliminado correctamente"))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deporte no encontrado"))
                } catch (e: Exception) {
                    // Posible error de llave foránea (si el deporte ya se usa en un torneo)
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "No se puede eliminar: El deporte está en uso"))
                }
            }
        }
    }
}