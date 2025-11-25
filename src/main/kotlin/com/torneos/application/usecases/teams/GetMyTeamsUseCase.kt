package com.torneos.application.usecases.teams

import com.torneos.domain.models.Team
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class GetMyTeamsUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(userId: UUID): List<Team> {
        return teamRepository.findByCaptain(userId)
    }
}