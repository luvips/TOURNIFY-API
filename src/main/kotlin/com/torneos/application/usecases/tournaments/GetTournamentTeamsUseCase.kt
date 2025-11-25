package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Team
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.enums.RegistrationStatus
import java.util.UUID

class GetTournamentTeamsUseCase(
    private val registrationRepository: RegistrationRepository,
    private val teamRepository: TeamRepository
) {
    suspend fun execute(tournamentId: UUID): List<Team> {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
            .filter { it.status == RegistrationStatus.approved }

        return registrations.mapNotNull { reg ->
            teamRepository.findById(reg.teamId)
        }
    }
}