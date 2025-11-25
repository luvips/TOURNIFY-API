package com.torneos.application.usecases.users

import com.torneos.domain.models.User
import com.torneos.domain.ports.UserRepository
import java.util.UUID

class UpdateUserProfileUseCase(private val userRepository: UserRepository) {
    suspend fun execute(userId: UUID, firstName: String?, lastName: String?, phone: String?): User {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        val updatedUser = user.copy(
            firstName = firstName ?: user.firstName,
            lastName = lastName ?: user.lastName,
            phone = phone ?: user.phone
        )

        return userRepository.update(updatedUser)
            ?: throw IllegalStateException("Error al actualizar perfil")
    }
}