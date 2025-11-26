package com.torneos.infrastructure.configuration

import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // 1. Koin
    install(Koin) {
        modules(getAppModule(environment.config))
    }

    // 2. 🔥 STATUS PAGES (EL DETECTOR DE ERRORES) 🔥
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Esto imprimirá el error ROJO en tu consola cuando falle el JSON
            println("🚨 ERROR 400 ATRAPADO: ${cause.message}")
            cause.printStackTrace()

            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Formato de petición inválido",
                "details" to (cause.message ?: "Error desconocido")
            ))
        }
    }

    // 3. Resto de configuraciones

    configureSerialization()
    configureSecurity()
    configureCors()
    DatabaseFactory.init(environment.config)
    configureRouting()
}