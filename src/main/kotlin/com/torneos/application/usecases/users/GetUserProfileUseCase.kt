package com.torneos.application.usecases.users

import com.torneos.domain.models.User
import com.torneos.domain.ports.UserRepository
import java.util.UUID

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend fun execute(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")
    }
}