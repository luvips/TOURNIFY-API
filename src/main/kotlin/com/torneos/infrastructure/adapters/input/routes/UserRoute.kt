package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.users.GetUserProfileUseCase
import com.torneos.application.usecases.users.UpdateUserProfileUseCase
import com.torneos.application.usecases.users.SwitchUserRoleUseCase
import com.torneos.application.usecases.users.UpdateUserAvatarUseCase // Asegúrate de importar esto
import com.torneos.infrastructure.adapters.input.dtos.UpdateProfileRequest
import com.torneos.infrastructure.adapters.input.dtos.SwitchRoleRequest
import com.torneos.infrastructure.adapters.input.dtos.AuthResponse
import com.torneos.infrastructure.adapters.input.mappers.toDto
import io.ktor.http.*
import io.ktor.http.content.* // Necesario para PartData
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
    // Inyectamos el caso de uso de avatar
    val updateUserAvatarUseCase by application.inject<UpdateUserAvatarUseCase>()

    route("/users") {
        authenticate("auth-jwt") {

            // ... (Tus rutas GET / PUT / POST switch-role se quedan igual) ...
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
            }

            // 4. Subir Avatar (CÓDIGO FINAL DE PRODUCCIÓN)
            post("/me/avatar") {
                val principal = call.principal<JWTPrincipal>()
                val userId = UUID.fromString(principal?.payload?.getClaim("id")?.asString())

                // Variables para almacenar los datos del archivo
                var fileName = ""
                var fileBytes: ByteArray? = null
                var contentType = "image/jpeg" // Valor por defecto

                try {
                    // Procesar la petición Multipart
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

                    // Llamar al caso de uso real
                    val avatarUrl = updateUserAvatarUseCase.execute(userId, fileName, fileBytes!!, contentType)

                    // Responder con la URL firmada para que el frontend pueda mostrarla inmediatamente
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "Avatar actualizado correctamente",
                        "avatarUrl" to avatarUrl
                    ))

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al procesar la subida: ${e.message}"))
                }
            }
        }
    }
}