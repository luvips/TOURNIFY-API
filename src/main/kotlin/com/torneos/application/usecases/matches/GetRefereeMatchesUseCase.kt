package com.torneos.application.usecases.matches

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import java.util.UUID

class GetRefereeMatchesUseCase(
    private val matchRepository: MatchRepository
) {
    suspend fun execute(refereeId: UUID): List<Match> {
        return matchRepository.findAll().filter { it.refereeId == refereeId }
    }
}
