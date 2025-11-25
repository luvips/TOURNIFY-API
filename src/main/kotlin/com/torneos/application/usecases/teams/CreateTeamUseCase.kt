package com.torneos.application.usecases.teams

import com.torneos.domain.models.Team
import com.torneos.domain.ports.TeamRepository

class CreateTeamUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(team: Team): Team {
        return teamRepository.create(team)
    }
}