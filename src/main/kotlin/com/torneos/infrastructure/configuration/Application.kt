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
    install(Koin) {
        modules(getAppModule(environment.config))
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            println(" ERROR 400 : ${cause.message}")
            cause.printStackTrace()

            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Formato de petición inválido",
                "details" to (cause.message ?: "Error desconocido")
            ))
        }
    }


    configureSerialization()
    configureSecurity()
    configureCors()
    DatabaseFactory.init(environment.config)
    configureRouting()
}