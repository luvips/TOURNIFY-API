package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.models.Team
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.enums.RegistrationStatus
import java.util.UUID

data class RegistrationWithTeamInfo(
    val registration: TeamRegistration,
    val team: Team?,
    val memberCount: Int
)

class GetTournamentRegistrationsUseCase(
    private val registrationRepository: RegistrationRepository,
    private val teamRepository: TeamRepository
) {
    suspend fun execute(tournamentId: UUID, status: RegistrationStatus? = null): List<RegistrationWithTeamInfo> {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        
        val filteredRegistrations = if (status != null) {
            registrations.filter { it.status == status }
        } else {
            registrations
        }
        
        return filteredRegistrations.map { registration ->
            val team = teamRepository.findById(registration.teamId)
            val memberCount = team?.let { teamRepository.getMembers(it.id).size } ?: 0
            RegistrationWithTeamInfo(registration, team, memberCount)
        }
    }
}
