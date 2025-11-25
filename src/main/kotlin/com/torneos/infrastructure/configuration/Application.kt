package com.torneos.infrastructure.configuration

import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // 1. Iniciar Koin (DI) antes que nada
    install(Koin) {
        modules(appModule)
    }

    // 2. Configurar Plugins
    configureSerialization()
    configureSecurity()

    // 3. Iniciar Base de Datos
    DatabaseFactory.init(environment.config)

    // 4. Registrar Rutas
    configureRouting()
}