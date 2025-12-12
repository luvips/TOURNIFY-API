package com.torneos.application.usecases.tournaments

import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

class ValidateUniquePlayersUseCase(
    private val registrationRepository: RegistrationRepository,
    private val teamRepository: TeamRepository
) {

    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
        val duplicatedPlayers: List<DuplicatedPlayerInfo> = emptyList(),
        val totalPlayersChecked: Int = 0,
        val totalTeamsChecked: Int = 0
    )

    data class DuplicatedPlayerInfo(
        val playerId: UUID,
        val playerName: String?,
        val teamsFound: List<UUID>
    )

    suspend fun execute(tournamentId: UUID): ValidationResult {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        
        if (registrations.isEmpty()) {
            return ValidationResult(
                isValid = true,
                message = "No hay equipos inscritos en el torneo",
                totalTeamsChecked = 0
            )
        }

        val playerToTeams = mutableMapOf<UUID, MutableList<UUID>>()

        val seenPlayers = mutableSetOf<UUID>()
        
        var totalPlayers = 0

        for (registration in registrations) {
            val teamId = registration.teamId
            val members = teamRepository.getMembers(teamId)

            for (member in members) {
                val playerId = member.userId ?: continue
                totalPlayers++

                if (!seenPlayers.add(playerId)) {

                    playerToTeams.getOrPut(playerId) { mutableListOf() }.add(teamId)
                } else {

                    playerToTeams[playerId] = mutableListOf(teamId)
                }
            }
        }

        val duplicatedPlayers = playerToTeams.entries
            .filter { it.value.size > 1 }
            .map { entry ->
                val playerId = entry.key
                val teams = entry.value

                val firstTeamId = teams.first()
                val members = teamRepository.getMembers(firstTeamId)
                val playerName = members.find { it.userId == playerId }?.memberName

                DuplicatedPlayerInfo(
                    playerId = playerId,
                    playerName = playerName,
                    teamsFound = teams
                )
            }

        val isValid = duplicatedPlayers.isEmpty()
        val message = if (isValid) {
            "Validación exitosa: No se encontraron jugadores duplicados"
        } else {
            "Validación fallida: Se encontraron ${duplicatedPlayers.size} jugadores en múltiples equipos"
        }

        return ValidationResult(
            isValid = isValid,
            message = message,
            duplicatedPlayers = duplicatedPlayers,
            totalPlayersChecked = totalPlayers,
            totalTeamsChecked = registrations.size
        )
    }


    suspend fun quickValidate(tournamentId: UUID): Boolean {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        val seenPlayers = mutableSetOf<UUID>()

        for (registration in registrations) {
            val members = teamRepository.getMembers(registration.teamId)
            
            for (member in members) {
                val playerId = member.userId ?: continue

                if (!seenPlayers.add(playerId)) {
                    return false
                }
            }
        }

        return true
    }


    suspend fun getAllUniquePlayers(tournamentId: UUID): Set<UUID> {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        val uniquePlayers = mutableSetOf<UUID>()

        for (registration in registrations) {
            val members = teamRepository.getMembers(registration.teamId)
            members.forEach { member ->
                member.userId?.let { uniquePlayers.add(it) }
            }
        }

        return uniquePlayers
    }


    suspend fun getPlayerStats(tournamentId: UUID): Map<String, Int> {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        val allPlayers = mutableListOf<UUID>()
        val uniquePlayers = mutableSetOf<UUID>()

        for (registration in registrations) {
            val members = teamRepository.getMembers(registration.teamId)
            members.forEach { member ->
                member.userId?.let {
                    allPlayers.add(it)
                    uniquePlayers.add(it)
                }
            }
        }

        return mapOf(
            "totalRegistrations" to allPlayers.size,
            "uniquePlayers" to uniquePlayers.size,
            "duplicates" to (allPlayers.size - uniquePlayers.size),
            "teams" to registrations.size
        )
    }
}
