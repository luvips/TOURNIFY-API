package com.torneos.application.usecases.users

import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.UserRepository
import java.util.UUID

class UpdateUserAvatarUseCase(
    private val userRepository: UserRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(userId: UUID, fileName: String, fileBytes: ByteArray): String {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        // 1. Subir archivo a S3
        // Retorna la "Key" (ej: "avatar-123.jpg"), NO la URL completa
        val objectKey = fileStorage.uploadFile(fileName, fileBytes, "image/jpeg") // O detecta el content-type

        // 2. Actualizar usuario con la Key
        val updatedUser = user.copy(avatarUrl = objectKey)
        userRepository.update(updatedUser)

        // 3. Retornar la URL firmada para que el frontend la muestre de inmediato
        return fileStorage.getPresignedUrl(objectKey)
    }
}