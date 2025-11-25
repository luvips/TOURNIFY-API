package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String, // El backend lo hasheará
    val firstName: String,
    val lastName: String,
    val role: UserRole = UserRole.player
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)