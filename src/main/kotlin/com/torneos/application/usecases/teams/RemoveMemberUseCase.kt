package com.torneos.application.usecases.teams

import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class RemoveMemberUseCase(private val teamRepository: TeamRepository) {
    suspend fun execute(teamId: UUID, memberId: UUID, requesterId: UUID) {
        val team = teamRepository.findById(teamId)
            ?: throw NoSuchElementException("Equipo no encontrado")


        if (team.captainId != requesterId) {
            throw SecurityException("Solo el capitán puede gestionar miembros")
        }

        val deleted = teamRepository.removeMember(memberId)
        if (!deleted) throw NoSuchElementException("Miembro no encontrado")
    }
}