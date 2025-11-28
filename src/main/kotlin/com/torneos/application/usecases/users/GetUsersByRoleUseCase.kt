package com.torneos.application.usecases.users

import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.User
import com.torneos.domain.ports.UserRepository

class GetUsersByRoleUseCase(
    private val userRepository: UserRepository
) {
    suspend fun execute(role: UserRole): List<User> {
        return userRepository.findByRole(role)
    }
}
