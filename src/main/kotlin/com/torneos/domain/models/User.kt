package com.torneos.domain.models

import com.torneos.domain.enums.UserRole
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val avatarUrl: String?,
    val isActive: Boolean,
    val emailVerified: Boolean,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)