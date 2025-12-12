package com.torneos.application.usecases.users

import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.User
import com.torneos.domain.ports.UserRepository
import com.torneos.domain.ports.AuthServicePort
import java.util.UUID

class SwitchUserRoleUseCase(
    private val userRepository: UserRepository,
    private val authService: AuthServicePort
) {

    suspend fun execute(userId: UUID, newRole: UserRole): Pair<String, User> {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        if (newRole !in listOf(UserRole.player, UserRole.organizer, UserRole.referee)) {
            throw IllegalArgumentException("Rol no válido")
        }

        val updatedUser = user.copy(role = newRole)
        
        val savedUser = userRepository.update(updatedUser)
            ?: throw IllegalStateException("Error al actualizar rol")

        val newToken = authService.generateToken(savedUser.id.toString(), savedUser.role.name)

        return Pair(newToken, savedUser)
    }
}
