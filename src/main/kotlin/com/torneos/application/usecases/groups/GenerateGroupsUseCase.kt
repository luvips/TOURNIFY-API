package com.torneos.application.usecases.groups

import com.torneos.domain.models.TournamentGroup
import com.torneos.domain.ports.TournamentGroupRepository
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class GenerateGroupsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val groupRepository: TournamentGroupRepository
) {
    suspend fun execute(tournamentId: UUID, userId: UUID): List<TournamentGroup> {
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")

        if (tournament.organizerId != userId) throw SecurityException("Solo el organizador puede generar grupos")
        if (!tournament.hasGroupStage) throw IllegalArgumentException("Este torneo no tiene fase de grupos")

        val numGroups = tournament.numberOfGroups ?: throw IllegalArgumentException("Número de grupos no definido")

        // Verificar si ya existen
        val existingGroups = groupRepository.findByTournament(tournamentId)
        if (existingGroups.isNotEmpty()) return existingGroups

        val newGroups = mutableListOf<TournamentGroup>()
        val groupNames = generateGroupNames(numGroups) // ["A", "B", "C"...]

        groupNames.forEachIndexed { index, name ->
            val group = TournamentGroup(
                id = UUID.randomUUID(),
                tournamentId = tournamentId,
                groupName = "Grupo $name",
                displayOrder = index + 1
            )
            newGroups.add(groupRepository.create(group))
        }

        return newGroups
    }

    private fun generateGroupNames(count: Int): List<String> {
        return (0 until count).map { index ->
            // Genera A, B, ... Z, AA, AB...
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            if (index < alphabet.length) alphabet[index].toString()
            else "G${index + 1}"
        }
    }
}