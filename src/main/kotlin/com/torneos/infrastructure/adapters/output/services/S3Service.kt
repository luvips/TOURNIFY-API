package com.torneos.infrastructure.adapters.output.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.ByteStream
import com.torneos.domain.ports.FileStoragePort
import io.ktor.server.config.*
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class S3Service(config: ApplicationConfig) : FileStoragePort {

    // Lectura de variables desde application.conf
    private val bucketName = config.property("aws.bucketName").getString()
    private val region = config.property("aws.region").getString()
    private val accessKey = config.property("aws.accessKey").getString()
    private val secretKey = config.property("aws.secretKey").getString()
    // Token opcional (solo si usas credenciales temporales en EC2/Lambda)
    private val sessionToken = config.propertyOrNull("aws.sessionToken")?.getString()

    /**
     * Proveedor de credenciales manual.
     * AWS SDK permite hacerlo automáticamente, pero así tenemos control total 
     * sobre las variables leídas de Ktor config.
     */
    private fun getCredentialsProvider(): CredentialsProvider {
        return object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials {
                return Credentials(accessKey, secretKey, sessionToken)
            }
        }
    }

    override suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String {
        // Generamos un nombre único para evitar colisiones (ej: avatar.png -> uuid-avatar.png)
        val uniqueKey = "${UUID.randomUUID()}-${fileName.replace(" ", "_")}"

        S3Client.fromEnvironment {
            this.region = this@S3Service.region
            this.credentialsProvider = getCredentialsProvider()
        }.use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = uniqueKey
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }
            s3.putObject(request)
        }
        
        return uniqueKey // Retornamos la KEY, no la URL completa (mejor práctica para portabilidad)
    }

    override suspend fun getPresignedUrl(objectKey: String): String {
        // Si el key es nulo o vacío, retornamos cadena vacía o url default
        if (objectKey.isBlank()) return ""
        
        // Si ya es una URL completa (http...), la devolvemos tal cual (legacy support)
        if (objectKey.startsWith("http")) return objectKey

        S3Client.fromEnvironment {
            this.region = this@S3Service.region
            this.credentialsProvider = getCredentialsProvider()
        }.use { s3 ->
            val request = GetObjectRequest {
                bucket = bucketName
                key = objectKey
            }
            
            // Generamos URL válida por 24 horas
            val presignedRequest = s3.presignGetObject(request, duration = 24.hours)
            return presignedRequest.url.toString()
        }
    }

    override suspend fun deleteFile(objectKey: String): Boolean {
        return try {
            S3Client.fromEnvironment {
                this.region = this@S3Service.region
                this.credentialsProvider = getCredentialsProvider()
            }.use { s3 ->
                val request = DeleteObjectRequest {
                    bucket = bucketName
                    key = objectKey
                }
                s3.deleteObject(request)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}