package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.GroupStanding
import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.ports.TournamentGroupRepository
import java.util.UUID

class GetTournamentStandingsUseCase(
    private val standingRepository: StandingRepository,
    private val groupRepository: TournamentGroupRepository
) {
    suspend fun execute(tournamentId: UUID): List<GroupStanding> {

        val groups = groupRepository.findByTournament(tournamentId)

        return groups.flatMap { group ->
            standingRepository.getStandingsByGroup(group.id)
        }
    }
}