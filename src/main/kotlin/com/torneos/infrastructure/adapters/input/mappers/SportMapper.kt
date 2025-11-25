package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Sport
import com.torneos.infrastructure.adapters.input.dtos.CreateSportRequest
import com.torneos.infrastructure.adapters.input.dtos.SportResponse
import java.util.UUID

fun CreateSportRequest.toDomain(): Sport {
    return Sport(
        name = this.name,
        category = this.category,
        defaultPlayersPerTeam = this.defaultPlayers,
        defaultMatchDuration = this.defaultDuration,
        icon = null, // Se maneja subida de imagen aparte
        isActive = true
    )
}

fun Sport.toResponse(): SportResponse {
    return SportResponse(
        id = this.id.toString(),
        name = this.name,
        category = this.category,
        iconUrl = this.icon
    )
}