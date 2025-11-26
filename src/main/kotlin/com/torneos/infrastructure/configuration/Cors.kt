package com.torneos.infrastructure.configuration

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        // 1. Permitir métodos HTTP comunes
        allowMethod(HttpMethod.Options) // Obligatorio para preflight requests del navegador
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // 2. Permitir encabezados necesarios (especialmente Auth para tu Token)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // 3. Configuración de Dominios (Orígenes)

        // OPCIÓN A (Desarrollo - Fácil): Permitir TODO
        anyHost()

        //  OPCIÓN B (Producción - Seguro): Permitir solo tu frontend
        // allowHost("localhost:4200") // Angular por defecto
        // allowHost("mi-frontend.com", schemes = listOf("http", "https"))

        // 4. Opcional: Si usas Cookies o Auth compleja
        // allowCredentials = true
    }
}