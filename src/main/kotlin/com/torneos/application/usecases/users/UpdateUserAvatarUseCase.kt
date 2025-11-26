package com.torneos.application.usecases.users

import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.UserRepository
import java.util.UUID

class UpdateUserAvatarUseCase(
    private val userRepository: UserRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(userId: UUID, fileName: String, fileBytes: ByteArray, contentType: String): String {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        val objectKey = fileStorage.uploadFile(fileName, fileBytes, contentType)

        val updatedUser = user.copy(avatarUrl = objectKey)
        userRepository.update(updatedUser)


        return fileStorage.getPresignedUrl(objectKey)
    }
}