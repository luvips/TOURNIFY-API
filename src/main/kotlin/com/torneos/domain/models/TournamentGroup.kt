package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class TournamentGroup(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val groupName: String,
    val displayOrder: Int,
    val createdAt: Instant = Instant.now()
)