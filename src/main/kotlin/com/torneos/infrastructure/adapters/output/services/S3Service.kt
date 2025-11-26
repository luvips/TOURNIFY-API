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

class S3Service(config: ApplicationConfig) : FileStoragePort {

    // Solo necesitamos el bucket y la región. ¡Adiós a las keys manuales!
    private val bucketName = config.property("aws.bucketName").getString()
    private val region = config.property("aws.region").getString()

    /**
     * Ya no necesitamos "getCredentialsProvider".
     * El SDK de Kotlin detectará automáticamente que está en una EC2
     * y usará el Rol IAM asignado. Magia pura. 🪄
     */

    override suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String {
        val uniqueKey = "${UUID.randomUUID()}-${fileName.replace(" ", "_")}"

        S3Client.fromEnvironment {
            this.region = this@S3Service.region
            // No seteamos credentialsProvider manualmente = Usa Default Chain (IAM Role)
        }.use { s3 ->
            val request = PutObjectRequest {
                bucket = bucketName
                key = uniqueKey
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }
            s3.putObject(request)
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
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}