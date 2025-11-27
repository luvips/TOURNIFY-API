package com.torneos.application.usecases.matches

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.Match
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.RegistrationRepository
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

/**
 * Caso de Uso: Generar Bracket de Eliminación
 * 
 * Genera automáticamente todos los partidos para un torneo de eliminación.
 * Soporta:
 * - Eliminación simple (single elimination)
 * - Eliminación doble (double elimination) - Pendiente implementación completa
 * 
 * Algoritmo:
 * 1. Obtiene equipos registrados y aprobados
 * 2. Calcula el número de rondas necesarias (potencia de 2)
 * 3. Genera bye matches si es necesario
 * 4. Crea los emparejamientos iniciales
 * 5. Crea los slots para rondas siguientes (TBD vs TBD)
 */
class GenerateBracketUseCase(
    private val matchRepository: MatchRepository,
    private val tournamentRepository: TournamentRepository,
    private val registrationRepository: RegistrationRepository
) {
    suspend fun execute(
        userId: UUID,
        tournamentId: UUID,
        startDate: Instant? = null
    ): List<Match> {
        // 1. Verificar que el torneo existe
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")
        
        // 2. Verificar que el usuario es el organizador
        if (tournament.organizerId != userId) {
            throw SecurityException("Solo el organizador puede generar el bracket")
        }
        
        // 3. Verificar que es torneo de eliminación
        if (tournament.tournamentType != "elimination") {
            throw IllegalArgumentException("Solo se pueden generar brackets para torneos de eliminación")
        }
        
        // 4. Obtener equipos registrados y aprobados
        val registrations = registrationRepository.findByTournamentId(tournamentId)
            .filter { it.status.name == "approved" }
        
        if (registrations.isEmpty()) {
            throw IllegalArgumentException("No hay equipos aprobados en el torneo")
        }
        
        if (registrations.size < 2) {
            throw IllegalArgumentException("Se necesitan al menos 2 equipos para generar el bracket")
        }
        
        // 5. Verificar si ya existen partidos
        val existingMatches = matchRepository.findByTournament(tournamentId)
        if (existingMatches.isNotEmpty()) {
            throw IllegalArgumentException("Ya existen partidos generados para este torneo")
        }
        
        // 6. Generar bracket según el modo de eliminación
        return when (tournament.eliminationMode) {
            "single" -> generateSingleEliminationBracket(tournamentId, registrations, startDate)
            "double" -> generateDoubleEliminationBracket(tournamentId, registrations, startDate)
            else -> throw IllegalArgumentException("Modo de eliminación no soportado: ${tournament.eliminationMode}")
        }
    }
    
    /**
     * Genera bracket de eliminación simple
     */
    private suspend fun generateSingleEliminationBracket(
        tournamentId: UUID,
        registrations: List<TeamRegistration>,
        startDate: Instant?
    ): List<Match> {
        val teams = registrations.map { it.teamId }
        val numTeams = teams.size
        
        // Calcular número de rondas (log2 redondeado hacia arriba)
        val numRounds = ceil(log2(numTeams.toDouble())).toInt()
        
        // Calcular número total de equipos en bracket perfecto (siguiente potencia de 2)
        val bracketSize = 2.0.pow(numRounds).toInt()
        
        // Número de byes necesarios
        val numByes = bracketSize - numTeams
        
        val matches = mutableListOf<Match>()
        var matchNumber = 1
        
        // --- RONDA 1: Crear partidos iniciales ---
        val roundName = getRoundName(numRounds, 1)
        val teamsShuffled = teams.shuffled() // Opcional: barajar equipos
        
        var teamIndex = 0
        val numFirstRoundMatches = bracketSize / 2
        
        for (matchInRound in 0 until numFirstRoundMatches) {
            val teamHome = if (teamIndex < numTeams) teamsShuffled[teamIndex++] else null
            val teamAway = if (teamIndex < numTeams) teamsShuffled[teamIndex++] else null
            
            // Si uno de los equipos es null, es un bye (el otro pasa automáticamente)
            val status = if (teamHome == null || teamAway == null) {
                MatchStatus.finished // Bye automático
            } else {
                MatchStatus.scheduled
            }
            
            // Si es bye, el ganador es el equipo que sí existe
            val winnerId = when {
                teamHome == null -> teamAway
                teamAway == null -> teamHome
                else -> null
            }
            
            val match = Match(
                id = UUID.randomUUID(),
                tournamentId = tournamentId,
                groupId = null,
                matchNumber = matchNumber++,
                roundName = roundName,
                roundNumber = 1,
                teamHomeId = teamHome,
                teamAwayId = teamAway,
                scheduledDate = startDate,
                location = null,
                refereeId = null,
                status = status,
                scoreHome = if (status == MatchStatus.finished && teamHome != null) 1 else null,
                scoreAway = if (status == MatchStatus.finished && teamAway != null) 1 else null,
                winnerId = winnerId,
                matchDataJson = "{}",
                notes = if (status == MatchStatus.finished) "Bye - Pase automático" else null,
                startedAt = null,
                finishedAt = if (status == MatchStatus.finished) Instant.now() else null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            matches.add(matchRepository.create(match))
        }
        
        // --- RONDAS SIGUIENTES: Crear slots vacíos (TBD vs TBD) ---
        for (round in 2..numRounds) {
            val roundName = getRoundName(numRounds, round)
            val numMatchesInRound = 2.0.pow(numRounds - round).toInt()
            
            for (matchInRound in 0 until numMatchesInRound) {
                val match = Match(
                    id = UUID.randomUUID(),
                    tournamentId = tournamentId,
                    groupId = null,
                    matchNumber = matchNumber++,
                    roundName = roundName,
                    roundNumber = round,
                    teamHomeId = null, // TBD - Se llenará cuando termine el partido anterior
                    teamAwayId = null, // TBD
                    scheduledDate = null,
                    location = null,
                    refereeId = null,
                    status = MatchStatus.scheduled,
                    scoreHome = null,
                    scoreAway = null,
                    winnerId = null,
                    matchDataJson = "{}",
                    notes = "Por definir - Ganadores de partidos anteriores",
                    startedAt = null,
                    finishedAt = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                matches.add(matchRepository.create(match))
            }
        }
        
        return matches
    }
    
    /**
     * Genera bracket de doble eliminación (Winners + Losers bracket)
     * TODO: Implementación completa pendiente
     */
    private suspend fun generateDoubleEliminationBracket(
        tournamentId: UUID,
        registrations: List<TeamRegistration>,
        startDate: Instant?
    ): List<Match> {
        // Por ahora, genera un bracket simple
        // La implementación completa requiere:
        // 1. Winners Bracket
        // 2. Losers Bracket (el doble de rondas - 1)
        // 3. Gran Final (Winner vs Loser winner)
        return generateSingleEliminationBracket(tournamentId, registrations, startDate)
    }
    
    /**
     * Obtiene el nombre de la ronda según su número
     */
    private fun getRoundName(totalRounds: Int, currentRound: Int): String {
        return when (totalRounds - currentRound) {
            0 -> "Final"
            1 -> "Semifinal"
            2 -> "Cuartos de Final"
            3 -> "Octavos de Final"
            else -> "Ronda ${currentRound}"
        }
    }
}
