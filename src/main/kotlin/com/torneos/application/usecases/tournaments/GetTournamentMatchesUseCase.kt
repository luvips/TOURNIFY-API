package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

data class MatchWithTeamNames(
    val match: Match,
    val homeTeamName: String?,
    val awayTeamName: String?
)

class GetTournamentMatchesUseCase(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository
) {
    suspend fun execute(tournamentId: UUID): List<MatchWithTeamNames> {
        val matches = matchRepository.findByTournament(tournamentId)
        
        return matches.map { match ->
            val homeTeamName = match.teamHomeId?.let { teamRepository.findById(it)?.name }
            val awayTeamName = match.teamAwayId?.let { teamRepository.findById(it)?.name }
            
            MatchWithTeamNames(
                match = match,
                homeTeamName = homeTeamName ?: "TBD",
                awayTeamName = awayTeamName ?: "TBD"
            )
        }
    }
}