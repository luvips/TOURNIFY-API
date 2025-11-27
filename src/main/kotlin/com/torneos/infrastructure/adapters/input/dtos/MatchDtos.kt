package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.MatchStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateMatchRequest(
    val teamHomeId: String?,      // UUID (puede ser null para TBD)
    val teamAwayId: String?,      // UUID (puede ser null para TBD)
    val scheduledDate: String?,   // ISO 8601
    val location: String?,
    val roundName: String?,       // "Final", "Semifinal", etc.
    val roundNumber: Int?,
    val matchNumber: Int?,
    val groupId: String?,         // UUID (si es fase de grupos)
    val refereeId: String?        // UUID
)

@Serializable
data class GenerateBracketRequest(
    val startDate: String?        // ISO 8601 - Fecha de inicio de los partidos
)

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