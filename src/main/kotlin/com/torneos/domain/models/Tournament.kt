package com.torneos.domain.models

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Tournament(
    val id: UUID,
    val name: String,
    val description: String?,
    val sportId: UUID,
    val sport: String?,
    val sportSubType: String?,
    val organizerId: UUID,
    val tournamentType: String,
    val category: String?,
    val eliminationMode: String?,
    val location: String?,
    val startDate: Instant,
    val endDate: Instant?,
    val registrationDeadline: Instant?,
    val maxTeams: Int,
    val currentTeams: Int,
    val registrationFee: BigDecimal,
    val prizePool: String?,
    val isPrivate: Boolean,
    val requiresApproval: Boolean,
    val accessCode: String?,
    val hasGroupStage: Boolean,
    val numberOfGroups: Int?,
    val teamsPerGroup: Int?,
    val teamsAdvancePerGroup: Int?,
    val sportSettingsJson: String,
    val allowTies: Boolean,
    val pointsForWin: Int,
    val pointsForDraw: Int,
    val pointsForLoss: Int,
    val rulesText: String?,
    val imageUrl: String?,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant
)