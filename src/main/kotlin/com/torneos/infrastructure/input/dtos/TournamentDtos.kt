package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.TournamentStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateTournamentRequest(
    val name: String,
    val description: String?,
    val organizerId: String, // UUID como String
    val sportId: String,     // UUID
    val sportName: String,   // Redundancia útil
    val sportSubType: String?,

    // Configuración
    val tournamentType: String,
    val category: String?,
    val eliminationMode: String?,

    // Fechas y Lugar
    val location: String?,
    val startDate: String,   // ISO Date String
    val endDate: String?,
    val registrationDeadline: String?,

    // Capacidad
    val maxTeams: Int,
    val registrationFee: Double,
    val prizePool: String?,

    // Privacidad
    val isPrivate: Boolean,
    val requiresApproval: Boolean,
    val accessCode: String?,

    // Fases
    val hasGroupStage: Boolean,
    val groupConfig: GroupConfigDto?,

    // Reglas
    val sportSettings: SportSettingsDto?,
    val rulesText: String?,

    // Puntuación
    val allowTies: Boolean,
    val pointsForWin: Int,
    val pointsForDraw: Int,
    val pointsForLoss: Int
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
    val substitutions: Int?,
    val extraTime: Boolean?,
    val penalties: Boolean?
)

@Serializable
data class TournamentResponse(
    val id: String,
    val name: String,
    val sport: String,
    val status: TournamentStatus,
    val startDate: String,
    val maxTeams: Int,
    val currentTeams: Int,
    val imageUrl: String?
)