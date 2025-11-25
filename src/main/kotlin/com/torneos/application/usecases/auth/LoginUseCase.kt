package com.torneos.application.usecases.auth

import com.torneos.domain.models.User
import com.torneos.domain.ports.AuthServicePort
import com.torneos.domain.ports.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository,
    private val authService: AuthServicePort
) {
    // Retorna Pair<Token, User> si es exitoso, o null si falla
    suspend fun execute(email: String, passwordRaw: String): Pair<String, User>? {
        val user = userRepository.findByEmail(email) ?: return null

        if (!authService.verifyPassword(passwordRaw, user.passwordHash)) {
            return null
        }

        val token = authService.generateToken(user.id.toString(), user.role.name)
        return Pair(token, user)
    }
}