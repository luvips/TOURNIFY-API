package com.torneos.application.usecases.matches

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.TeamRepository
import java.time.Instant
import java.util.UUID


class CreateMatchUseCase(
    private val matchRepository: MatchRepository,
    private val tournamentRepository: TournamentRepository,
    private val teamRepository: TeamRepository
) {
    suspend fun execute(
        userId: UUID,
        tournamentId: UUID,
        teamHomeId: UUID?,
        teamAwayId: UUID?,
        scheduledDate: Instant?,
        location: String?,
        roundName: String?,
        roundNumber: Int?,
        matchNumber: Int?,
        groupId: UUID?,
        refereeId: UUID?
    ): Match {
        // 1. Verificar que el torneo existe
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")
        
        // 2. Verificar que el usuario es el organizador
        if (tournament.organizerId != userId) {
            throw SecurityException("Solo el organizador puede crear partidos")
        }
        
        // 3. Validar equipos
        if (teamHomeId != null) {
            val homeTeam = teamRepository.findById(teamHomeId)
                ?: throw IllegalArgumentException("Equipo local no encontrado")
        }
        
        if (teamAwayId != null) {
            val awayTeam = teamRepository.findById(teamAwayId)
                ?: throw IllegalArgumentException("Equipo visitante no encontrado")
        }
        
        // 4. Validar que no sean el mismo equipo
        if (teamHomeId != null && teamAwayId != null && teamHomeId == teamAwayId) {
            throw IllegalArgumentException("Un equipo no puede jugar contra sí mismo")
        }
        
        // 5. Crear el partido
        val match = Match(
            id = UUID.randomUUID(),
            tournamentId = tournamentId,
            groupId = groupId,
            matchNumber = matchNumber,
            roundName = roundName,
            roundNumber = roundNumber,
            teamHomeId = teamHomeId,
            teamAwayId = teamAwayId,
            scheduledDate = scheduledDate,
            location = location,
            refereeId = refereeId,
            status = MatchStatus.scheduled,
            scoreHome = null,
            scoreAway = null,
            winnerId = null,
            matchDataJson = "{}",
            notes = null,
            startedAt = null,
            finishedAt = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        return matchRepository.create(match)
    }
}
