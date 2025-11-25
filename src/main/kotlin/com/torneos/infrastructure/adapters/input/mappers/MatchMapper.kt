package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Match
import com.torneos.infrastructure.adapters.input.dtos.MatchResponse

fun Match.toResponse(): MatchResponse {
    // Nota: El modelo Match de dominio solo tiene IDs de equipos.
    // Para mostrar nombres reales, idealmente el UseCase debería devolver un modelo enriquecido.
    // Por ahora devolvemos los IDs o nulos para que compile y funcione.
    return MatchResponse(
        id = this.id.toString(),
        homeTeamName = this.teamHomeId?.toString() ?: "TBD", // O "Equipo A"
        awayTeamName = this.teamAwayId?.toString() ?: "TBD", // O "Equipo B"
        scoreHome = this.scoreHome,
        scoreAway = this.scoreAway,
        status = this.status,
        scheduledDate = this.scheduledDate?.toString()
    )
}
