package com.torneos.application.usecases.matches

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

data class MatchDetails(
    val match: Match,
    val homeTeamName: String?,
    val awayTeamName: String?,
    val tournamentName: String?
)

class GetMatchDetailsUseCase(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val tournamentRepository: TournamentRepository
) {
    suspend fun execute(matchId: UUID): MatchDetails {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")
        
        val homeTeamName = match.teamHomeId?.let { teamRepository.findById(it)?.name }
        val awayTeamName = match.teamAwayId?.let { teamRepository.findById(it)?.name }
        val tournamentName = tournamentRepository.findById(match.tournamentId)?.name
        
        return MatchDetails(
            match = match,
            homeTeamName = homeTeamName ?: "TBD",
            awayTeamName = awayTeamName ?: "TBD",
            tournamentName = tournamentName
        )
    }
}