package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.enums.EliminationMode
import com.torneos.domain.enums.TournamentStatus
import com.torneos.domain.models.Tournament
import com.torneos.infrastructure.adapters.input.dtos.CreateTournamentRequest
import com.torneos.infrastructure.adapters.input.dtos.TournamentResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

fun CreateTournamentRequest.toDomain(organizerId: UUID): Tournament {
    return Tournament(
        organizerId = organizerId,
        sportId = UUID.fromString(this.sportId),
        name = this.name,
        description = this.description,
        
        // TODO: Idealmente buscar el nombre del deporte en BD, por ahora usamos placeholder o vacío
        sport = "Deporte", 
        sportSubType = this.sportSubType,
        
        tournamentType = this.tournamentType,
        category = this.category,
        eliminationMode = this.eliminationMode?.let { EliminationMode.valueOf(it) },
        
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
        numberOfGroups = this.groupConfig?.numberOfGroups,
        teamsPerGroup = this.groupConfig?.teamsPerGroup,
        teamsAdvancePerGroup = this.groupConfig?.teamsAdvancePerGroup,
        
        // Convertimos los DTOs anidados a JSON String para guardarlos en BD
        sportSettingsJson = this.sportSettings?.let { Json.encodeToString(it) } ?: "{}",
        groupConfigJson = this.groupConfig?.let { Json.encodeToString(it) } ?: "{}",
        
        allowTies = this.allowTies,
        pointsForWin = this.pointsForWin,
        pointsForDraw = this.pointsForDraw,
        pointsForLoss = this.pointsForLoss,
        
        rulesText = this.rulesText,
        imageUrl = null, // Se puede actualizar luego con subida de imagen
        status = TournamentStatus.upcoming
    )
}

fun Tournament.toResponse(): TournamentResponse {
    return TournamentResponse(
        id = this.id.toString(),
        name = this.name,
        sportId = this.sportId.toString(),
        status = this.status,
        startDate = this.startDate.toString(),
        maxTeams = this.maxTeams,
        currentTeams = this.currentTeams,
        imageUrl = this.imageUrl,
        organizerId = this.organizerId.toString()
    )
}