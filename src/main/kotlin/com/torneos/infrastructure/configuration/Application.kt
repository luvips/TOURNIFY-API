package com.torneos.infrastructure.configuration

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

// 1. Entry Point
fun main(args: Array<String>): Unit = EngineMain.main(args)

// 2. Módulo Principal
fun Application.module() {
    // Configuración JSON básica
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    // Ruta de prueba temporal para verificar el commit
    routing {
        get("/") {
            call.respondText("Tournify Backend API")
        }
    }

    println("Corriendo sistema")
}