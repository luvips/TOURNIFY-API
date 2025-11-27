package com.torneos.application.usecases.teams

import com.torneos.domain.models.Team
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class UpdateTeamUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(teamId: UUID, updatedTeam: Team, requesterId: UUID): Team {
        // 1. Verificar que el equipo existe
        val existingTeam = teamRepository.findById(teamId)
            ?: throw NoSuchElementException("Equipo no encontrado")
        
        // 2. Validar que solo el capitán pueda actualizar el equipo
        if (existingTeam.captainId != requesterId) {
            throw SecurityException("Solo el capitán puede actualizar el equipo")
        }
        
        // 3. Actualizar el equipo
        return teamRepository.update(updatedTeam)
            ?: throw IllegalStateException("No se pudo actualizar el equipo")
    }
}
