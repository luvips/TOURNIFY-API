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
    val createTournamentUseCase by application.inject<CreateTournamentUseCase>()
    val getTournamentsUseCase by application.inject<GetTournamentsUseCase>()
    val getTournamentDetailsUseCase by application.inject<GetTournamentDetailsUseCase>()
    val getTournamentStandingsUseCase by application.inject<GetTournamentStandingsUseCase>()
    val getTournamentMatchesUseCase by application.inject<GetTournamentMatchesUseCase>()
    val getTournamentTeamsUseCase by application.inject<GetTournamentTeamsUseCase>()
    val joinTournamentUseCase by application.inject<JoinTournamentUseCase>()
    val followTournamentUseCase by application.inject<FollowTournamentUseCase>()
    val unfollowTournamentUseCase by application.inject<UnfollowTournamentUseCase>()
    val updateTournamentUseCase by application.inject<UpdateTournamentUseCase>()
    route("/tournaments") {

        // 1. Listar Torneos
        get {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val tournaments = getTournamentsUseCase.execute(page, size)
                call.respond(HttpStatusCode.OK, tournaments.map { it.toResponse() })
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error desconocido")))
            }
        }

        // 2. Detalle de Torneo
        get("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Falta el ID del torneo"))
                return@get
            }

            try {
                val tournament = getTournamentDetailsUseCase.execute(UUID.fromString(idParam))
                call.respond(HttpStatusCode.OK, tournament.toResponse())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de torneo inválido"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Torneo no encontrado"))
            }
        }

        authenticate("auth-jwt") {
            // 3. Crear Torneo (POST) - ¡AQUÍ ESTABA EL PROBLEMA 400!
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("id")?.asString()
                val userRole = principal?.payload?.getClaim("role")?.asString()

                println("📝 [CREATE TOURNAMENT] Usuario: $userIdStr | Rol: $userRole")

                if (userIdStr == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido"))
                }

                // Permitir crear torneo a 'organizer' y 'admin'
                if (userRole !in listOf("organizer", "admin")) {
                    println("⛔ [CREATE TOURNAMENT] Acceso denegado. Rol actual: $userRole")
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo organizadores pueden crear torneos. Tu rol es: $userRole"))
                }

                try {
                    // 1. Intentar recibir el JSON
                    val request = call.receive<CreateTournamentRequest>()
                    println("📦 [CREATE TOURNAMENT] JSON Recibido: $request")

                    // 2. Convertir a Dominio (Aquí suelen fallar las Fechas o UUIDs)
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))
                    println("⚙️ [CREATE TOURNAMENT] Dominio convertido exitosamente")

                    // 3. Ejecutar Caso de Uso (Aquí puede fallar si el sportId no existe en BD)
                    val created = createTournamentUseCase.execute(domainTournament)

                    println("✅ [CREATE TOURNAMENT] Torneo creado: ${created.id}")
                    call.respond(HttpStatusCode.Created, created.toResponse())

                } catch (e: SerializationException) {
                    println("❌ [ERROR JSON] El formato del JSON es incorrecto: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "JSON mal formado o tipo de dato incorrecto (ej. enviaste texto en un número).",
                        "details" to e.message
                    ))
                } catch (e: DateTimeParseException) {
                    println("❌ [ERROR FECHA] Formato de fecha inválido: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Formato de fecha inválido. Se espera ISO-8601 (ej: 2025-12-01T10:00:00Z).",
                        "details" to e.message
                    ))
                } catch (e: IllegalArgumentException) {
                    println("❌ [ERROR ARGUMENTO] UUID o Dato inválido: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "error" to "Datos inválidos (UUID incorrecto o SportID no existe).",
                        "details" to e.message
                    ))
                } catch (e: Exception) {
                    println("❌ [ERROR GENERAL] Excepción no controlada:")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Error interno del servidor.",
                        "details" to e.message
                    ))
                }
            }

            put("/{id}") {
                val tournamentIdStr = call.parameters["id"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

                if (tournamentIdStr == null || userIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID o Token inválido"))
                    return@put
                }

                try {
                    // 1. Recibir datos nuevos
                    val request = call.receive<CreateTournamentRequest>()

                    // 2. Convertir a dominio (Usamos el ID del usuario como 'organizer' temporalmente para el mapper)
                    val domainTournament = request.toDomain(organizerId = UUID.fromString(userIdStr))

                    // 3. Ejecutar actualización
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
                    println("❌ Error en PUT: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Datos inválidos", "details" to e.message))
                }
            }

            // 4. Unirse a Torneo
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
        }
    }
}