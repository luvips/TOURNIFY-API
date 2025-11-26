package com.torneos.infrastructure.adapters.input.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateTournamentRequest(
    val name: String,
    val description: String? = null,
    val sportId: String, // UUID como String
    val sportSubType: String? = null,
    val tournamentType: String,
    val category: String? = null,
    val eliminationMode: String? = null,
    val location: String? = null,
    val startDate: String, // ISO 8601 String: "2025-12-01T10:00:00Z"
    val endDate: String? = null,
    val registrationDeadline: String? = null,
    val maxTeams: Int,
    val registrationFee: Double = 0.0,
    val prizePool: String? = null,
    val isPrivate: Boolean = false,
    val requiresApproval: Boolean = false,
    val accessCode: String? = null,
    val hasGroupStage: Boolean = false,
    val numberOfGroups: Int? = null,
    val teamsPerGroup: Int? = null,
    val teamsAdvancePerGroup: Int? = null,
    val sportSettings: String? = null, // JSON como String
    val allowTies: Boolean = false,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val pointsForLoss: Int = 0,
    val rulesText: String? = null
)

@Serializable
data class TournamentResponse(
    val id: String,
    val name: String,
    val description: String?,
    val sportId: String,
    val sport: String?,
    val organizerId: String,
    val tournamentType: String,
    val status: String,
    val startDate: String,
    val maxTeams: Int,
    val currentTeams: Int,
    val imageUrl: String?
)

@Serializable
data class JoinTournamentRequest(
    val teamId: String
)