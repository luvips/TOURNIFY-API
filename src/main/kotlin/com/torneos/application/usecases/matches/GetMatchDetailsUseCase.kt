package com.torneos.application.usecases.matches

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.UserRepository
import java.util.UUID

data class RefereeDetails(
    val id: UUID,
    val username: String
)

data class MatchDetails(
    val match: Match,
    val homeTeamName: String?,
    val awayTeamName: String?,
    val tournamentName: String?,
    val referee: RefereeDetails?
)

class GetMatchDetailsUseCase(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository
) {
    suspend fun execute(matchId: UUID): MatchDetails {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")
        
        val homeTeamName = match.teamHomeId?.let { teamRepository.findById(it)?.name }
        val awayTeamName = match.teamAwayId?.let { teamRepository.findById(it)?.name }
        val tournamentName = tournamentRepository.findById(match.tournamentId)?.name
        val referee = match.refereeId?.let { userRepository.findById(it) }
            ?.let { RefereeDetails(id = it.id, username = it.username) }
        
        return MatchDetails(
            match = match,
            homeTeamName = homeTeamName ?: "TBD",
            awayTeamName = awayTeamName ?: "TBD",
            tournamentName = tournamentName,
            referee = referee
        )
    }
}