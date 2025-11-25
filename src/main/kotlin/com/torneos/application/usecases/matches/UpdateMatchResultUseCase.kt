package com.torneos.application.usecases.matches

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import java.time.Instant
import java.util.UUID

class UpdateMatchResultUseCase(private val matchRepository: MatchRepository) {
    suspend fun execute(
        matchId: UUID,
        userId: UUID,
        scoreHome: Int,
        scoreAway: Int,
        status: MatchStatus,
        winnerId: UUID?
    ): Match {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")

        // Aquí podrías validar si userId es el árbitro del partido

        val updatedMatch = match.copy(
            scoreHome = scoreHome,
            scoreAway = scoreAway,
            status = status,
            winnerId = winnerId,
            finishedAt = if (status == MatchStatus.finished) Instant.now() else match.finishedAt,
            updatedAt = Instant.now()
        )

        return matchRepository.update(updatedMatch)
            ?: throw IllegalStateException("Error al guardar")
    }
}