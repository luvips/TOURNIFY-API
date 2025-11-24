package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class Match(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val teamHomeId: UUID?,
    val teamAwayId: UUID?,
    val startTime: Instant?,
    val location: String?,
    val scoreHome: Int,
    val scoreAway: Int,
    val status: String, // 'scheduled', 'live', etc.
    val stageName: String?,
    val roundNumber: Int?,
    val matchDetailsJson: String = "{}", // JSONB
    val createdAt: Instant = Instant.now()
)