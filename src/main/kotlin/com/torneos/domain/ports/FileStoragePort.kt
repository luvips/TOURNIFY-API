package com.torneos.domain.ports

interface FileStoragePort {

    suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String


    suspend fun getPresignedUrl(objectKey: String): String
    

    suspend fun deleteFile(objectKey: String): Boolean

}