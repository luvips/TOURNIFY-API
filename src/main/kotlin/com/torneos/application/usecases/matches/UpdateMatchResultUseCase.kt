package com.torneos.application.usecases.matches

import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import java.time.Instant
import java.util.UUID

class UpdateMatchResultUseCase(
    private val matchRepository: MatchRepository,
    private val standingRepository: StandingRepository)
{


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


        val updatedMatch = match.copy(
            scoreHome = scoreHome,
            scoreAway = scoreAway,
            status = status,
            winnerId = winnerId,
            finishedAt = if (status == MatchStatus.finished) Instant.now() else match.finishedAt,
            updatedAt = Instant.now()
        )

        val savedMatch = matchRepository.update(updatedMatch)
            ?: throw IllegalStateException("Error al guardar")


        if (savedMatch.groupId != null && savedMatch.status == MatchStatus.finished) {
            standingRepository.updateStandings(savedMatch.groupId)
        }

        return savedMatch
    }


}