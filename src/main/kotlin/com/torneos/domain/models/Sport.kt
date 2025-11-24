package com.torneos.domain.models

import com.torneos.domain.enums.SportCategory
import java.time.Instant
import java.util.UUID

data class Sport(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: SportCategory,
    val icon: String?,
    val defaultPlayersPerTeam: Int?,
    val defaultMatchDuration: Int?,
    val isActive: Boolean,
    val createdAt: Instant = Instant.now()
)