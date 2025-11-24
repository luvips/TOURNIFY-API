package com.torneos.domain.models

import com.torneos.domain.enums.EliminationMode
import com.torneos.domain.enums.TournamentStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Tournament(
    val id: UUID = UUID.randomUUID(),
    val organizerId: UUID,
    val name: String,
    val description: String?,
    val location: String?,
    val imageUrl: String?,
    val sport: String,
    val sportSubtype: String?,
    val category: String?,
    val tournamentType: String,
    val eliminationMode: EliminationMode,
    val startDate: Instant,
    val endDate: Instant?,
    val status: TournamentStatus,
    val maxTeams: Int,
    val registrationFee: BigDecimal,
    val prizePool: String?,
    val isPrivate: Boolean,
    val accessCode: String?,
    val requiresApproval: Boolean,
    // JSONB: Los mantenemos como String por ahora para flexibilidad
    val sportSettingsJson: String = "{}",
    val scoringRulesJson: String = "{}",
    val stageConfigJson: String = "{}",
    val rulesText: String?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)