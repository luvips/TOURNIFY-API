package com.torneos.application.usecases.teams

import com.torneos.domain.models.TeamWithMembers
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class GetTeamDetailsUseCase(private val teamRepository: TeamRepository) {
    
    suspend fun execute(teamId: UUID, userId: UUID): TeamWithMembers {
        // 1. Verificar que el equipo existe
        val teamWithMembers = teamRepository.getTeamWithMembers(teamId)
            ?: throw NoSuchElementException("Equipo no encontrado")
        
        // 2. Verificar que el usuario es miembro del equipo
        val isMember = teamRepository.isMemberOfTeam(teamId, userId)
        if (!isMember) {
            throw SecurityException("No tienes permiso para ver los detalles de este equipo")
        }
        
        return teamWithMembers
    }
}
