package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class Team(
    val id: UUID = UUID.randomUUID(),
    val captainId: UUID?,
    val name: String,
    val shortName: String?,
    val logoUrl: String?,
    val description: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val isActive: Boolean,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)