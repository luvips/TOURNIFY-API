package com.torneos.infrastructure.adapters.input.routes

import com.torneos.application.usecases.auth.LoginUseCase
import com.torneos.application.usecases.auth.RegisterUserUseCase
import com.torneos.infrastructure.adapters.input.dtos.AuthResponse
import com.torneos.infrastructure.adapters.input.dtos.LoginRequest
import com.torneos.infrastructure.adapters.input.dtos.RegisterRequest
import com.torneos.infrastructure.adapters.input.mappers.toDomain
import com.torneos.infrastructure.adapters.input.mappers.toDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val loginUseCase by application.inject<LoginUseCase>()
    val registerUseCase by application.inject<RegisterUserUseCase>()

    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                // Log para debugging (remover en producción)
                println(" [REGISTER] Intento de registro para: ${request.email}")

                val user = registerUseCase.execute(request.toDomain(), request.password)

                println(" [REGISTER] Usuario creado exitosamente: ${user.id}")
                call.respond(HttpStatusCode.Created, user.toDto())

            } catch (e: IllegalArgumentException) {
                println(" [REGISTER] Error de validación: ${e.message}")
                call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Datos inválidos")))

            } catch (e: org.jetbrains.exposed.exceptions.ExposedSQLException) {
                println(" [REGISTER] Error de Exposed SQL:")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Error de base de datos",
                    "details" to e.message,
                    "type" to "ExposedSQLException"
                ))

            } catch (e: org.postgresql.util.PSQLException) {
                println(" [REGISTER] Error de PostgreSQL:")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Error de conexión a PostgreSQL",
                    "details" to e.message,
                    "sqlState" to e.sqlState,
                    "type" to "PSQLException"
                ))

            } catch (e: Exception) {
                println(" [REGISTER] Error inesperado: ${e.javaClass.name}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Error interno del servidor",
                    "type" to e.javaClass.simpleName,
                    "message" to (e.message ?: "Error desconocido"),
                    "stackTrace" to e.stackTrace.take(5).map { it.toString() }
                ))
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                println(" [LOGIN] Intento de login para: ${request.email}")

                val result = loginUseCase.execute(request.email, request.password)

                if (result != null) {
                    val (token, user) = result
                    println(" [LOGIN] Login exitoso para: ${user.email}")
                    call.respond(HttpStatusCode.OK, AuthResponse(token, user.toDto()))
                } else {
                    println(" [LOGIN] Credenciales inválidas para: ${request.email}")
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
                }

            } catch (e: Exception) {
                println(" [LOGIN] Error inesperado: ${e.javaClass.name}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Error en el proceso de login",
                    "details" to e.message
                ))
            }
        }
    }
}