package com.torneos.application.usecases.auth

import com.torneos.domain.models.User
import com.torneos.domain.ports.AuthServicePort
import com.torneos.domain.ports.UserRepository

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val authService: AuthServicePort
) {
    suspend fun execute(user: User, passwordRaw: String): User {
        if (userRepository.findByEmail(user.email) != null) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val passwordHash = authService.hashPassword(passwordRaw)
        val newUser = user.copy(passwordHash = passwordHash)

        return userRepository.create(newUser)
    }
}