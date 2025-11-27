package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.MatchStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateMatchRequest(
    val teamHomeId: String?,
    val teamAwayId: String?,
    val scheduledDate: String?,
    val location: String?,
    val roundName: String?,
    val roundNumber: Int?,
    val matchNumber: Int?,
    val groupId: String?,
    val refereeId: String?
)

@Serializable
data class GenerateBracketRequest(
    val startDate: String?
)

@Serializable
data class UpdateMatchResultRequest(
    val scoreHome: Int,
    val scoreAway: Int,
    val status: MatchStatus,
    val winnerId: String?
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