package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import java.util.UUID

class GetTournamentMatchesUseCase(private val matchRepository: MatchRepository) {
    suspend fun execute(tournamentId: UUID): List<Match> {
        return matchRepository.findByTournament(tournamentId)
    }
}