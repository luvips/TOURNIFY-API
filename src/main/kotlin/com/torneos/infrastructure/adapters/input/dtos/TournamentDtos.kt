package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.TournamentStatus
import kotlinx.serialization.Serializable

// --- REQUESTS (Lo que envía Angular) ---

@Serializable
data class CreateTournamentRequest(
    val name: String,
    val description: String?,
    val sportId: String,        // UUID
    val sportSubType: String?,

    val tournamentType: String, // 'single_elimination', 'group_stage'
    val category: String?,
    val eliminationMode: String?,

    val location: String?,
    val startDate: String,      // ISO Date
    val endDate: String?,
    val registrationDeadline: String?,

    val maxTeams: Int,
    val registrationFee: Double,
    val prizePool: String?,

    val isPrivate: Boolean,
    val requiresApproval: Boolean,
    val accessCode: String?,

    // Configuración Compleja (JSONB)
    val hasGroupStage: Boolean,
    val groupConfig: GroupConfigDto?,
    val sportSettings: SportSettingsDto?,

    val allowTies: Boolean,
    val pointsForWin: Int,
    val pointsForDraw: Int,
    val pointsForLoss: Int,

    val rulesText: String?
)

@Serializable
data class GroupConfigDto(
    val numberOfGroups: Int,
    val teamsPerGroup: Int,
    val teamsAdvancePerGroup: Int
)

@Serializable
data class SportSettingsDto(
    val matchDuration: Int?,
    val halves: Int?,
    val playersPerTeam: Int?,
    val extraTime: Boolean?,
    val penalties: Boolean?
)
@Serializable
data class JoinTournamentRequest(
    val teamId: String
)

// --- RESPONSES (Lo que devuelves a Angular) ---

@Serializable
data class TournamentResponse(
    val id: String,
    val name: String,
    val sportId: String,
    val status: TournamentStatus,
    val startDate: String,
    val maxTeams: Int,
    val currentTeams: Int,
    val imageUrl: String?,
    val organizerId: String
)