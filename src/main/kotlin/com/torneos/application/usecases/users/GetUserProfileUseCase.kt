package com.torneos.application.usecases.users

import com.torneos.domain.models.User
import com.torneos.domain.ports.FileStoragePort // Importar
import com.torneos.domain.ports.UserRepository
import java.util.UUID

class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(userId: UUID): User {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        val signedUrl = user.avatarUrl?.let { key ->
            fileStorage.getPresignedUrl(key)
        }

        // Retornamos el usuario con la URL temporal en lugar de la Key
        return user.copy(avatarUrl = signedUrl)
    }
}