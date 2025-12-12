
import com.torneos.application.usecases.teams.CreateTeamUseCase
import com.torneos.application.usecases.teams.AddMemberUseCase
import com.torneos.application.usecases.teams.DeleteTeamUseCase
import com.torneos.application.usecases.teams.GetMyTeamsUseCase
import com.torneos.application.usecases.teams.GetTeamDetailsUseCase
import com.torneos.application.usecases.teams.RemoveMemberUseCase
import com.torneos.application.usecases.teams.UpdateTeamUseCase
import com.torneos.infrastructure.adapters.input.dtos.CreateTeamRequest
import com.torneos.infrastructure.adapters.input.dtos.UpdateTeamRequest
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
    val createTeamUseCase by application.inject<CreateTeamUseCase>()
    val getMyTeamsUseCase by application.inject<GetMyTeamsUseCase>()
    val getTeamDetailsUseCase by application.inject<GetTeamDetailsUseCase>()
    val addMemberUseCase by application.inject<AddMemberUseCase>()
    val deleteTeamUseCase by application.inject<DeleteTeamUseCase>()
    val removeMemberUseCase by application.inject<RemoveMemberUseCase>()
    val updateTeamUseCase by application.inject<UpdateTeamUseCase>()

    route("/teams") {
        authenticate("auth-jwt") {

            get("/my") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                val teams = getMyTeamsUseCase.execute(userId)
                call.respond(HttpStatusCode.OK, teams.map { it.toResponse() })
            }

            // Obtener detalles de un equipo con sus miembros
            get("/{id}") {
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())
                
                val teamIdStr = call.parameters["id"]
                
                if (teamIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))
                    return@get
                }
                
                try {
                    val teamId = UUID.fromString(teamIdStr)
                    val teamWithMembers = getTeamDetailsUseCase.execute(teamId, userId)
                    call.respond(HttpStatusCode.OK, teamWithMembers.toResponse())
                } catch (e: IllegalArgumentException) {
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Equipo no encontrado"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                }
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
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())
                val teamIdStr = call.parameters["id"]
                
                if (teamIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))
                    return@post
                }
                
                try {
                    val teamId = UUID.fromString(teamIdStr)
                    val request = call.receive<AddMemberRequest>()
                    
                    // Validar que el usuario actual es el capitán del equipo
                    val team = getTeamDetailsUseCase.execute(teamId, userId)
                    if (team.team.captainId != userId) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo el capitán puede agregar miembros"))
                        return@post
                    }
                    
                    val member = addMemberUseCase.execute(request.toDomain(teamId))
                    call.respond(HttpStatusCode.Created, member.toResponse())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "No encontrado")))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                }
            }
            
            // Actualizar equipo
            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())
                val teamIdStr = call.parameters["id"]
                
                if (teamIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))
                    return@put
                }
                
                try {
                    val request = call.receive<UpdateTeamRequest>()
                    
                    val existingTeamWithMembers = getTeamDetailsUseCase.execute(teamId, userId)
                    val existingTeam = existingTeamWithMembers.team
                    
                    val updatedTeam = request.toDomain(teamId, existingTeam)
                    
                    val result = updateTeamUseCase.execute(teamId, updatedTeam, userId)
                    call.respond(HttpStatusCode.OK, result.toResponse())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de equipo inválido"))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Equipo no encontrado"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                }
            }
            
            delete("/{id}") {
                val teamIdStr = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                try {
                    deleteTeamUseCase.execute(UUID.fromString(teamIdStr), UUID.fromString(userIdStr))
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Equipo eliminado"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Error")))
                }
            }

            // ELIMINAR MIEMBRO (Expulsar jugador)
            delete("/{id}/members/{memberId}") {
                val teamIdStr = call.parameters["id"]
                val memberIdStr = call.parameters["memberId"]
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

                if (teamIdStr == null || memberIdStr == null || userIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                try {
                    removeMemberUseCase.execute(
                        teamId = UUID.fromString(teamIdStr),
                        memberId = UUID.fromString(memberIdStr),
                        requesterId = UUID.fromString(userIdStr)
                    )
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Miembro eliminado"))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Error")))
                }
            }
        }

    }
}