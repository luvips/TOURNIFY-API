package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class Team(
    val id: UUID = UUID.randomUUID(),
    val captainId: UUID?,
    val name: String,
    val logoUrl: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val createdAt: Instant = Instant.now()
)

data class TeamMember(
    val id: UUID = UUID.randomUUID(),
    val teamId: UUID,
    val userId: UUID?,
    val playerName: String,
    val jerseyNumber: Int?,
    val position: String?
)