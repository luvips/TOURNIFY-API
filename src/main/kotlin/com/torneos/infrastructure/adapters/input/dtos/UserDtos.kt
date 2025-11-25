package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val role: UserRole,
    val avatarUrl: String?
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val phone: String?
)