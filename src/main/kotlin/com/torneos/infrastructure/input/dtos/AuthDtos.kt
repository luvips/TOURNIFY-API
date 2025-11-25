package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val passwordHash: String, // El frontend debe enviar el hash o el backend hashearlo (usualmente raw password aquí)
    val role: UserRole,
    val firstName: String?,
    val lastName: String?,
    val phone: String?
)

@Serializable
data class LoginRequest(
    val email: String,
    val passwordHash: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val role: UserRole
)