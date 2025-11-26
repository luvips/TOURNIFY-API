package com.torneos.infrastructure.adapters.output.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.ByteStream
import com.torneos.domain.ports.FileStoragePort
import io.ktor.server.config.*
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import org.slf4j.LoggerFactory

class S3Service(config: ApplicationConfig) : FileStoragePort {

    private val logger = LoggerFactory.getLogger(S3Service::class.java)
    private val bucketName = config.property("aws.bucketName").getString()
    private val region = config.property("aws.region").getString()
    // Se eliminaron accessKey y secretKey explícitos

    override suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String {
        val uniqueKey = "${UUID.randomUUID()}-${fileName.replace(" ", "_")}"

        // S3Client.fromEnvironment detectará automáticamente las credenciales (ej. Rol IAM)
        S3Client.fromEnvironment {
            this.region = this@S3Service.region
        }.use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = uniqueKey
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }
            s3.putObject(request)
            logger.info("Archivo subido exitosamente a S3: $uniqueKey")
        }

        return uniqueKey
    }

    override suspend fun getPresignedUrl(objectKey: String): String {
        if (objectKey.isBlank()) return ""
        if (objectKey.startsWith("http")) return objectKey

        S3Client.fromEnvironment {
            this.region = this@S3Service.region
        }.use { s3 ->
            val request = GetObjectRequest {
                bucket = bucketName
                key = objectKey
            }

            // Genera una URL firmada válida por 24 horas
            val presignedRequest = s3.presignGetObject(request, duration = 24.hours)
            return presignedRequest.url.toString()
        }
    }

    override suspend fun deleteFile(objectKey: String): Boolean {
        return try {
            S3Client.fromEnvironment {
                this.region = this@S3Service.region
            }.use { s3 ->
                val request = DeleteObjectRequest {
                    bucket = bucketName
                    key = objectKey
                }
                s3.deleteObject(request)
                logger.info("Archivo eliminado de S3: $objectKey")
            }
            true
        } catch (e: Exception) {
            logger.error("Error al eliminar archivo de S3: $objectKey", e)
            false
        }
    }
}