package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.MatchStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMatchResultRequest(
    val scoreHome: Int,
    val scoreAway: Int,
    val status: MatchStatus, // 'finished', 'live'
    val winnerId: String?    // UUID del equipo ganador (si aplica)
)

@Serializable
data class MatchResponse(
    val id: String,
    val homeTeamName: String?,
    val awayTeamName: String?,
    val scoreHome: Int?,
    val scoreAway: Int?,
    val status: MatchStatus,
    val scheduledDate: String?
)