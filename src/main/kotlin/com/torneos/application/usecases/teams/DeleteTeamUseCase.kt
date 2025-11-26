package com.torneos.application.usecases.teams

import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class DeleteTeamUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(teamId: UUID, requesterId: UUID) {
        val team = teamRepository.findById(teamId)
            ?: throw NoSuchElementException("Equipo no encontrado")

        // Validar que solo el capitán pueda borrar el equipo
        if (team.captainId != requesterId) {
            throw SecurityException("Solo el capitán puede eliminar el equipo")
        }

        val deleted = teamRepository.delete(teamId)
        if (!deleted) throw IllegalStateException("No se pudo eliminar el equipo")
    }
}