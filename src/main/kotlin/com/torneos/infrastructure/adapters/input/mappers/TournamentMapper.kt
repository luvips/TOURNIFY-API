package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Tournament
import com.torneos.application.usecases.tournaments.RegistrationWithTeamInfo
import com.torneos.infrastructure.adapters.input.dtos.CreateTournamentRequest
import com.torneos.infrastructure.adapters.input.dtos.TournamentResponse
import com.torneos.infrastructure.adapters.input.dtos.TeamRegistrationResponse
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

fun CreateTournamentRequest.toDomain(organizerId: UUID): Tournament {
    return Tournament(
        id = UUID.randomUUID(),
        organizerId = organizerId,
        sportId = UUID.fromString(this.sportId),
        name = this.name,
        description = this.description,
        sport = "", // El caso de uso se encargará de rellenar esto
        sportSubType = this.sportSubType,
        tournamentType = this.tournamentType,
        category = this.category,
        eliminationMode = this.eliminationMode,
        location = this.location,
        startDate = Instant.parse(this.startDate),
        endDate = this.endDate?.let { Instant.parse(it) },
        registrationDeadline = this.registrationDeadline?.let { Instant.parse(it) },
        maxTeams = this.maxTeams,
        currentTeams = 0,
        registrationFee = BigDecimal.valueOf(this.registrationFee),
        prizePool = this.prizePool,
        isPrivate = this.isPrivate,
        requiresApproval = this.requiresApproval,
        accessCode = this.accessCode,
        hasGroupStage = this.hasGroupStage,
        numberOfGroups = this.numberOfGroups,
        teamsPerGroup = this.teamsPerGroup,
        teamsAdvancePerGroup = this.teamsAdvancePerGroup,
        sportSettingsJson = this.sportSettings ?: "{}",
        allowTies = this.allowTies,
        pointsForWin = this.pointsForWin,
        pointsForDraw = this.pointsForDraw,
        pointsForLoss = this.pointsForLoss,
        rulesText = this.rulesText,
        imageUrl = null,
        status = "upcoming",
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}

fun Tournament.toResponse(): TournamentResponse {
    return TournamentResponse(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        sportId = this.sportId.toString(),
        sport = this.sport,
        organizerId = this.organizerId.toString(),
        tournamentType = this.tournamentType,
        status = this.status,
        startDate = this.startDate.toString(),
        maxTeams = this.maxTeams,
        currentTeams = this.currentTeams,
        imageUrl = this.imageUrl
    )
}

fun RegistrationWithTeamInfo.toResponse(): TeamRegistrationResponse {
    return TeamRegistrationResponse(
        id = this.registration.id.toString(),
        tournamentId = this.registration.tournamentId.toString(),
        teamId = this.registration.teamId.toString(),
        teamName = this.team?.name ?: "Unknown Team",
        teamLogoUrl = this.team?.logoUrl,
        status = this.registration.status.name,
        registrationDate = this.registration.registrationDate.toString(),
        approvedAt = this.registration.approvedAt?.toString()
    )
}