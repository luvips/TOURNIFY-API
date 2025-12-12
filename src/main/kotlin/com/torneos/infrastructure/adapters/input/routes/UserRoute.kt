package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.users.GetUserProfileUseCase
import com.torneos.application.usecases.users.UpdateUserProfileUseCase
import com.torneos.application.usecases.users.SwitchUserRoleUseCase
import com.torneos.application.usecases.users.UpdateUserAvatarUseCase
import com.torneos.application.usecases.users.GetUsersByRoleUseCase
import com.torneos.domain.enums.UserRole
import com.torneos.infrastructure.adapters.input.dtos.UpdateProfileRequest
import com.torneos.infrastructure.adapters.input.dtos.SwitchRoleRequest
import com.torneos.infrastructure.adapters.input.dtos.AuthResponse
import com.torneos.infrastructure.adapters.input.mappers.toDto
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userRoutes() {
    val getUserProfileUseCase by application.inject<GetUserProfileUseCase>()
    val updateUserProfileUseCase by application.inject<UpdateUserProfileUseCase>()
    val switchUserRoleUseCase by application.inject<SwitchUserRoleUseCase>()
    val updateUserAvatarUseCase by application.inject<UpdateUserAvatarUseCase>()
    val getUsersByRoleUseCase by application.inject<GetUsersByRoleUseCase>()

        authenticate("auth-jwt") {

            // 1. Ver mi perfil
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                try {
                    val user = getUserProfileUseCase.execute(userId)
                    call.respond(HttpStatusCode.OK, user.toDto())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Usuario no encontrado"))
            }

            // 2. Actualizar perfil
            put("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                val request = call.receive<UpdateProfileRequest>()

                try {
                    val updatedUser = updateUserProfileUseCase.execute(
                        userId,
                        request.firstName,
                        request.lastName,
                        request.phone
                    )
                    call.respond(HttpStatusCode.OK, updatedUser.toDto())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Error al actualizar"))
            }

            // 3. Cambiar de rol
            post("/me/switch-role") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                try {
                    val request = call.receive<SwitchRoleRequest>()
                    val (newToken, updatedUser) = switchUserRoleUseCase.execute(userId, request.role)

                    call.respond(HttpStatusCode.OK, AuthResponse(
                        token = newToken,
                        user = updatedUser.toDto()
                    ))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al cambiar rol"))
            }

            // 4. Subir Avatar
            post("/me/avatar") {
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                // Variables para almacenar los datos del archivo
                var fileName = ""
                var fileBytes: ByteArray? = null
                var contentType = "image/jpeg"

                try {
                    val multipart = call.receiveMultipart()

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            fileName = part.originalFileName ?: "avatar.jpg"
                            contentType = part.contentType?.toString() ?: "image/jpeg"
                            fileBytes = part.streamProvider().readBytes()
                        }
                        part.dispose()
                    }

                    if (fileBytes == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se envió ninguna imagen"))
                        return@post
                    }
                    val avatarUrl = updateUserAvatarUseCase.execute(userId, fileName, fileBytes!!, contentType)

                    // Responder con la URL firmada para que el frontend pueda mostrarla
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "Avatar actualizado correctamente",
                        "avatarUrl" to avatarUrl
                    ))

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al procesar la subida: ${e.message}"))
            }

            // 5. Obtener usuarios por rol (para asignar árbitros, etc.)
            get {
                val roleParam = call.request.queryParameters["role"]
                
                if (roleParam == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetro 'role' requerido"))
                    return@get
                }
                
                try {
                    val role = UserRole.valueOf(roleParam.lowercase())
                    val users = getUsersByRoleUseCase.execute(role)
                    call.respond(HttpStatusCode.OK, users.map { it.toDto() })
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Rol inválido. Valores permitidos: player, organizer, referee, admin"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener usuarios"))
                }
            }
        }
    }
}