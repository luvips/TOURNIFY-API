package com.torneos.application.usecases.matches

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import java.util.UUID

class GetMatchDetailsUseCase(private val matchRepository: MatchRepository) {
    suspend fun execute(matchId: UUID): Match {
        return matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")
    }
}