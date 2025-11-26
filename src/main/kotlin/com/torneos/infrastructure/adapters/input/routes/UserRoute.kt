package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.users.GetUserProfileUseCase
import com.torneos.application.usecases.users.UpdateUserAvatarUseCase
import com.torneos.application.usecases.users.UpdateUserProfileUseCase
import com.torneos.infrastructure.adapters.input.dtos.UpdateProfileRequest
import com.torneos.infrastructure.adapters.input.mappers.toDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.http.content.*
import java.util.UUID

fun Route.userRoutes() {
    val getUserProfileUseCase by application.inject<GetUserProfileUseCase>()
    val updateUserProfileUseCase by application.inject<UpdateUserProfileUseCase>()
    val updateUserAvatarUseCase by application.inject<UpdateUserAvatarUseCase>()
    route("/users") {
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
            

            // 3. Subir Avatar (Placeholder para S3)
            post("/me/avatar") {
                val userIdStr = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                if (userIdStr == null) return@post call.respond(HttpStatusCode.Unauthorized)

                // Leer el multipart
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName = ""

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName = part.originalFileName ?: "avatar.jpg"
                        fileBytes = part.streamProvider().readBytes()
                    }
                    part.dispose()
                }

                if (fileBytes != null) {
                    try {
                        // Ejecutamos el caso de uso
                        val signedUrl = updateUserAvatarUseCase.execute(
                            userId = UUID.fromString(userIdStr),
                            fileName = fileName,
                            fileBytes = fileBytes!!
                        )
                        // Devolvemos la URL lista para usar en el <img src="..."> del frontend
                        call.respond(HttpStatusCode.OK, mapOf("avatarUrl" to signedUrl))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al subir imagen"))
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se envió ningún archivo"))
                }
            }
        }

    }
}