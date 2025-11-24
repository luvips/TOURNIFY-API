package com.torneos.domain.models

import com.torneos.domain.enums.EliminationMode
import com.torneos.domain.enums.TournamentStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Tournament(
    val id: UUID = UUID.randomUUID(),
    val organizerId: UUID,
    val sportId: UUID?, // Puede ser null si borras el deporte, aunque en SQL pusimos restrict
    
    // Info Básica
    val name: String,
    val description: String?,
    val sport: String, // String redundante para UI rápida
    val sportSubType: String?,

    // Configuración
    val tournamentType: String,
    val category: String?,
    val eliminationMode: EliminationMode?,

    // Fechas y Lugar
    val location: String?,
    val startDate: Instant,
    val endDate: Instant?,
    val registrationDeadline: Instant?,

    // Capacidad y Costos
    val maxTeams: Int,
    val currentTeams: Int,
    val registrationFee: BigDecimal,
    val prizePool: String?,

    // Privacidad
    val isPrivate: Boolean,
    val requiresApproval: Boolean,
    val accessCode: String?,

    // Grupos y Config JSON (Como String)
    val hasGroupStage: Boolean,
    val numberOfGroups: Int?,
    val teamsPerGroup: Int?,
    val teamsAdvancePerGroup: Int?,
    
    val sportSettingsJson: String = "{}", // JSONB
    val groupConfigJson: String = "{}",   // JSONB

    // Puntuación
    val allowTies: Boolean,
    val pointsForWin: Int,
    val pointsForDraw: Int,
    val pointsForLoss: Int,

    // Media
    val rulesText: String?,
    val imageUrl: String?,
    
    val status: TournamentStatus,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)