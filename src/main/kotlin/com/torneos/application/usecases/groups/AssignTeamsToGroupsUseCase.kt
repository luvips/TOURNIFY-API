package com.torneos.application.usecases.groups

import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TournamentGroupRepository
import com.torneos.domain.enums.RegistrationStatus
import java.util.UUID

class AssignTeamsToGroupsUseCase(
    private val registrationRepository: RegistrationRepository,
    private val groupRepository: TournamentGroupRepository
) {
    suspend fun execute(tournamentId: UUID, method: String = "random") {
        // 1. Obtener grupos
        val groups = groupRepository.findByTournament(tournamentId)
        if (groups.isEmpty()) throw IllegalStateException("Primero debes generar los grupos")

        // 2. Obtener inscripciones aprobadas
        val registrations = registrationRepository.findByTournamentId(tournamentId)
            .filter { it.status == RegistrationStatus.approved }
            .toMutableList()

        if (registrations.isEmpty()) throw IllegalStateException("No hay equipos aprobados")

        // 3. Barajar si es random
        if (method == "random") {
            registrations.shuffle()
        }

        // 4. Distribuir (Round Robin allocation)
        // Equipo 1 -> Grupo A, Equipo 2 -> Grupo B, Equipo 3 -> Grupo A...
        var groupIndex = 0
        registrations.forEach { reg ->
            val targetGroup = groups[groupIndex]

            // Actualizar el groupId en la inscripción
            val updatedReg = reg.copy(groupId = targetGroup.id)
            registrationRepository.update(updatedReg)

            groupIndex = (groupIndex + 1) % groups.size
        }
    }
}