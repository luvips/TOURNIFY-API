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
        // 1. Obtener los grupos del torneo
        val groups = groupRepository.findByTournament(tournamentId)

        // 2. Obtener la tabla de cada grupo y unirlas
        return groups.flatMap { group ->
            standingRepository.getStandingsByGroup(group.id)
        }
    }
}