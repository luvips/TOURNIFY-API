package com.torneos.application.usecases.teams

import com.torneos.domain.models.TeamMember
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.UserRepository // Importar esto

class AddMemberUseCase(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {
    suspend fun execute(member: TeamMember): TeamMember {
        var memberToSave = member

        if (!member.memberEmail.isNullOrBlank()) {
            val existingUser = userRepository.findByEmail(member.memberEmail)

            if (existingUser != null) {

                memberToSave = member.copy(userId = existingUser.id)
            }
        }


        return teamRepository.addMember(memberToSave)
    }
}