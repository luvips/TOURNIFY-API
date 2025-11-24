package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.users.GetUserProfileUseCase
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
import java.util.UUID

fun Route.userRoutes() {
    val getUserProfileUseCase by inject<GetUserProfileUseCase>()
    val updateUserProfileUseCase by inject<UpdateUserProfileUseCase>()

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
                // Aquí iría la lógica de Multipart para subir imagen a S3Service
                call.respond(HttpStatusCode.OK, mapOf("message" to "Avatar actualizado (Simulado)"))
            }
        }
    }
}