package com.torneos.domain.models

import com.torneos.domain.enums.MatchStatus
import java.time.Instant
import java.util.UUID

data class Match(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val groupId: UUID?,
    
    val matchNumber: Int?,
    val roundName: String?,
    val roundNumber: Int?,
    
    val teamHomeId: UUID?,
    val teamAwayId: UUID?,
    
    val scheduledDate: Instant?,
    val location: String?,
    val refereeId: UUID?,
    
    val status: MatchStatus,
    val scoreHome: Int?,
    val scoreAway: Int?,
    val winnerId: UUID?,
    
    val matchDataJson: String = "{}", // JSONB
    val notes: String?,
    
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)