package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class MatchResult(
    val id: UUID = UUID.randomUUID(),
    val matchId: UUID,
    val teamId: UUID,
    val playerId: UUID?,
    
    val eventType: String, // 'goal', 'red_card', etc.
    val eventTime: Int?,
    val eventPeriod: String?,
    
    val eventDataJson: String = "{}", // JSONB
    val notes: String?,
    
    val createdAt: Instant = Instant.now()
)