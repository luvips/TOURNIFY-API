package com.torneos.application.usecases.tournaments

import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TeamRepository
import java.util.UUID

/**
 * Caso de uso para validar que no haya jugadores duplicados en un torneo.
 * Demuestra el uso de CONJUNTO (Set) para detectar duplicados eficientemente.
 * 
 * Útil antes de iniciar un torneo para asegurar la integridad de las inscripciones.
 */
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

    /**
     * Valida que ningún jugador esté inscrito en múltiples equipos del mismo torneo
     * Usa SET para detección eficiente de duplicados en O(n)
     */
    suspend fun execute(tournamentId: UUID): ValidationResult {
        // Obtener todos los equipos inscritos en el torneo
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        
        if (registrations.isEmpty()) {
            return ValidationResult(
                isValid = true,
                message = "No hay equipos inscritos en el torneo",
                totalTeamsChecked = 0
            )
        }

        // Mapa para rastrear en qué equipos aparece cada jugador
        val playerToTeams = mutableMapOf<UUID, MutableList<UUID>>()
        
        // SET para rastrear jugadores ya vistos (uso de CONJUNTO)
        val seenPlayers = mutableSetOf<UUID>()
        
        var totalPlayers = 0

        // Iterar sobre cada equipo inscrito
        for (registration in registrations) {
            val teamId = registration.teamId
            val members = teamRepository.getMembers(teamId)

            for (member in members) {
                val playerId = member.userId ?: continue
                totalPlayers++

                // Agregar jugador al set y verificar si ya existía
                if (!seenPlayers.add(playerId)) {
                    // El jugador ya estaba en el set, es un duplicado
                    playerToTeams.getOrPut(playerId) { mutableListOf() }.add(teamId)
                } else {
                    // Primera vez que vemos este jugador
                    playerToTeams[playerId] = mutableListOf(teamId)
                }
            }
        }

        // Filtrar solo los jugadores que están en más de un equipo
        val duplicatedPlayers = playerToTeams.entries
            .filter { it.value.size > 1 }
            .map { entry ->
                val playerId = entry.key
                val teams = entry.value
                
                // Obtener nombre del jugador (de cualquier equipo donde esté)
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

    /**
     * Validación rápida: solo retorna true/false sin detalles
     */
    suspend fun quickValidate(tournamentId: UUID): Boolean {
        val registrations = registrationRepository.findByTournamentId(tournamentId)
        val seenPlayers = mutableSetOf<UUID>()

        for (registration in registrations) {
            val members = teamRepository.getMembers(registration.teamId)
            
            for (member in members) {
                val playerId = member.userId ?: continue
                
                // Si add() retorna false, el jugador ya existía (duplicado)
                if (!seenPlayers.add(playerId)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Obtener el conjunto de todos los jugadores únicos en un torneo
     */
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

    /**
     * Estadísticas de jugadores por torneo
     */
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
