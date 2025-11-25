package com.torneos.application.usecases.teams

import com.torneos.domain.models.TeamMember
import com.torneos.domain.ports.TeamRepository

class AddMemberUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(member: TeamMember): TeamMember {
        return teamRepository.addMember(member)
    }
}