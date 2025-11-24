package com.torneos.domain.ports

interface FileStoragePort {
    /**
     * Sube un archivo y retorna la Key (identificador único) o la URL.
     */
    suspend fun uploadFile(fileName: String, fileBytes: ByteArray, contentType: String): String

    /**
     * Genera una URL firmada (temporal) para ver un archivo privado.
     */
    suspend fun getPresignedUrl(objectKey: String): String
    
    /**
     * Elimina un archivo (opcional, pero útil).
     */
    suspend fun deleteFile(objectKey: String): Boolean

}