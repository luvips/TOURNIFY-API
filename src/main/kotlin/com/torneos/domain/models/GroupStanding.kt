package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class GroupStanding(
    val id: UUID = UUID.randomUUID(),
    val groupId: UUID,
    val teamId: UUID,
    
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    
    val position: Int?,
    val updatedAt: Instant = Instant.now()
)