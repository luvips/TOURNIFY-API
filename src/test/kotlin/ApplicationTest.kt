package com

import com.torneos.infrastructure.configuration.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.* // Importante para MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        // 👇 Configuramos el entorno del test manualmente
        environment {
            config = MapApplicationConfig(
                "storage.driverClassName" to "org.postgresql.Driver",
                "storage.jdbcUrl" to "jdbc:postgresql://100.25.51.198:5432/Tournify_BD", // Usa tu IP/DB real
                "storage.username" to "postgres",
                "storage.password" to "Tournify", // ⚠️ Pon la contraseña real aquí para el test

                "jwt.secret" to "secret-test",
                "jwt.domain" to "https://api.tournify.com",
                "jwt.audience" to "tournify-users",
                "jwt.realm" to "Tournify App",

                "aws.bucketName" to "test-bucket",
                "aws.region" to "us-east-1",
                "aws.accessKey" to "test",
                "aws.secretKey" to "test"
            )
        }

        application {
            module()
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}