package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class TournamentFollower(
    val userId: UUID,
    val tournamentId: UUID,
    val followedAt: Instant = Instant.now()
)