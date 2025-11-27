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
import kotlinx.serialization.SerializationException
import org.koin.ktor.ext.inject
import java.time.format.DateTimeParseException
import java.util.UUID

fun Route.tournamentRoutes() {
    // Inyección de dependencias
    val createTournamentUseCase by application.inject<CreateTournamentUseCase>()
    val getTournamentsUseCase by application.inject<GetTournamentsUseCase>()
    val getTournamentDetailsUseCase by application.inject<GetTournamentDetailsUseCase>()
    val updateTournamentUseCase by application.inject<UpdateTournamentUseCase>()
    val deleteTournamentUseCase by application.inject<DeleteTournamentUseCase>()
    val joinTournamentUseCase by application.inject<JoinTournamentUseCase>()
    val followTournamentUseCase by application.inject<FollowTournamentUseCase>()
    val unfollowTournamentUseCase by application.inject<UnfollowTournamentUseCase>()
    val getFollowedTournamentsUseCase by application.inject<GetFollowedTournamentsUseCase>()
    val getMyTournamentsUseCase by application.inject<GetMyTournamentsUseCase>()
    val getTournamentMatchesUseCase by application.inject<GetTournamentMatchesUseCase>()
    val getTournamentRegistrationsUseCase by application.inject<GetTournamentRegistrationsUseCase>()
    val approveRegistrationUseCase by application.inject<ApproveRegistrationUseCase>()
    val rejectRegistrationUseCase by application.inject<RejectRegistrationUseCase>()

    // (Otros use cases como standings/teams se pueden inyectar si se usan)

    route("/tournaments") {

        // 1. Listar Torneos (Público)
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val tournaments = getTournamentsUseCase.execute(page, size)
                call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error desconocido")))
            }
        }

        // 2. Ver Detalle (Público)
        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val tournament = getTournamentDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, tournament.toResponse())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Torneo no encontrado"))
            }
        }

        // 2.1. Obtener Partidos de un Torneo (Público)
        get("/{id}/matches") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                val matches = getTournamentMatchesUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, matches.map { it.toResponse() })
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener partidos"))
            }
        }

        // 2.2. Obtener Registraciones de un Torneo (Público)
        get("/{id}/registrations") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val statusParam = call.request.queryParameters["status"]
            
            try {
                val status = statusParam?.let { 
                    try {
                        com.torneos.domain.enums.RegistrationStatus.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val registrations = getTournamentRegistrationsUseCase.execute(UUID.fromString(idParam), status)
                call.respond(HttpStatusCode.OK, registrations.map { it.toResponse() })
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener registraciones"))
            }
        }

        authenticate("auth-jwt") {

            // 3. Crear Torneo
            post {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val userRole = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()

                if (userIdStr == null) return@post call.respond(HttpStatusCode.Unauthorized)

                if (userRole !in listOf("organizer", "admin")) {
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Rol no autorizado"))
                }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    val created = createTournamentUseCase.execute(domainTournament)
                    call.respond(HttpStatusCode.Created, created.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error al crear")))
                }
            }

            // 4. Editar Torneo (PUT)
            put("/{id}") {
                val tournamentIdStr = call.parameters["id"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

                if (tournamentIdStr == null || userIdStr == null) {
                    return@put call.respond(HttpStatusCode.BadRequest)
                }

                try {
                    val request = call.receive<CreateTournamentRequest>()
                    // Convertimos a dominio usando el ID del usuario actual como organizer temporalmente
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))

                    val updated = updateTournamentUseCase.execute(
                        id = UUID.fromString(tournamentIdStr),
                        tournament = domainTournament,
                        requesterId = UUID.fromString(userIdStr)
                    )
                    call.respond(HttpStatusCode.OK, updated.toResponse())
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error al actualizar")))
                }
            }

            // 5. Eliminar Torneo (DELETE)
            delete("/{id}") {
                val tournamentIdStr = call.parameters["id"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

                if (tournamentIdStr == null || userIdStr == null) {
                    return@delete call.respond(HttpStatusCode.BadRequest)
                }

                try {
                    deleteTournamentUseCase.execute(
                        id = UUID.fromString(tournamentIdStr),
                        requesterId = UUID.fromString(userIdStr)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Torneo eliminado correctamente"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al eliminar")))
                }
            }

            // 6. Unirse a Torneo (Join)
            post("/{id}/join") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]
                if (userIdStr == null || tournamentIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val request = call.receive<JoinTournamentRequest>()
                    joinTournamentUseCase.execute(
                        userId = UUID.fromString(userIdStr),
                        tournamentId = UUID.fromString(tournamentIdStr),
                        teamId = UUID.fromString(request.teamId)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Inscripción enviada"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Error")))
                }
            }

            // 7. Seguir Torneo (Follow)
            post("/{id}/follow") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]

                if (userIdStr == null || tournamentIdStr == null) return@post call.respond(HttpStatusCode.BadRequest)

                try {
                    followTournamentUseCase.execute(UUID.fromString(userIdStr), UUID.fromString(tournamentIdStr))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Ahora sigues este torneo"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Error al seguir")))
                }
            }

            // 8. Dejar de Seguir (Unfollow)
            delete("/{id}/follow") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["id"]

                if (userIdStr == null || tournamentIdStr == null) return@delete call.respond(HttpStatusCode.BadRequest)

                try {
                    unfollowTournamentUseCase.execute(UUID.fromString(userIdStr), UUID.fromString(tournamentIdStr))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Dejaste de seguir este torneo"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error al dejar de seguir")))
                }
            }

            // 9. Obtener Torneos Seguidos (Jugadores)
            get("/followed") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                
                if (userIdStr == null) return@get call.respond(HttpStatusCode.Unauthorized)

                try {
                    val tournaments = getFollowedTournamentsUseCase.execute(UUID.fromString(userIdStr))
                    call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al obtener torneos seguidos")))
                }
            }

            // 10. Obtener Mis Torneos (Organizadores)
            get("/my-tournaments") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                
                if (userIdStr == null) return@get call.respond(HttpStatusCode.Unauthorized)

                try {
                    val tournaments = getMyTournamentsUseCase.execute(UUID.fromString(userIdStr))
                    call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al obtener tus torneos")))
                }
            }

            // 11. Aprobar Registración
            post("/{tournamentId}/registrations/{registrationId}/approve") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["tournamentId"]
                val registrationIdStr = call.parameters["registrationId"]

                if (userIdStr == null || tournamentIdStr == null || registrationIdStr == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetros inválidos"))
                }

                try {
                    approveRegistrationUseCase.execute(
                        tournamentId = UUID.fromString(tournamentIdStr),
                        registrationId = UUID.fromString(registrationIdStr),
                        approverId = UUID.fromString(userIdStr)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Registración aprobada exitosamente"))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al aprobar registración")))
                }
            }

            // 12. Rechazar Registración
            post("/{tournamentId}/registrations/{registrationId}/reject") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                val tournamentIdStr = call.parameters["tournamentId"]
                val registrationIdStr = call.parameters["registrationId"]

                if (userIdStr == null || tournamentIdStr == null || registrationIdStr == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetros inválidos"))
                }

                try {
                    val body = try {
                        call.receive<Map<String, String>>()
                    } catch (e: Exception) {
                        emptyMap()
                    }
                    val reason = body["reason"]
                    
                    rejectRegistrationUseCase.execute(
                        tournamentId = UUID.fromString(tournamentIdStr),
                        registrationId = UUID.fromString(registrationIdStr),
                        reason = reason
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Registración rechazada"))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error al rechazar registración")))
                }
            }
        }
    }
}