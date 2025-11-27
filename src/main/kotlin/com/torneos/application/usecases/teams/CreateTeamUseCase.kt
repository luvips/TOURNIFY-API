package com.torneos.application.usecases.teams

import com.torneos.domain.enums.MemberRole
import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.UserRepository

class CreateTeamUseCase(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) {
    suspend fun execute(team: Team): Team {
        // 1. Crear el equipo
        val createdTeam = teamRepository.create(team)
        
        // 2. Agregar al capitán automáticamente como miembro
        if (createdTeam.captainId != null) {
            // Obtener información del usuario para el nombre
            val captain = userRepository.findById(createdTeam.captainId)
            val captainName = if (captain != null) {
                "${captain.firstName ?: ""} ${captain.lastName ?: ""}".trim()
            } else null
            
            val captainMember = TeamMember(
                teamId = createdTeam.id,
                userId = createdTeam.captainId,
                memberName = captainName.takeIf { !it.isNullOrBlank() },
                memberEmail = captain?.email,
                memberPhone = null,
                role = MemberRole.captain,
                jerseyNumber = null,
                position = null,
                isActive = true
            )
            teamRepository.addMember(captainMember)
        }
        
        return createdTeam
    }
}