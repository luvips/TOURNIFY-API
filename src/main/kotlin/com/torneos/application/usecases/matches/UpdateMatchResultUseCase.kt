package com.torneos.application.usecases.matches

import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.UserRepository
import java.time.Instant
import java.util.UUID

class UpdateMatchResultUseCase(
    private val matchRepository: MatchRepository,
    private val standingRepository: StandingRepository,
    private val userRepository: UserRepository,
    private val tournamentRepository: TournamentRepository
) {

    suspend fun execute(
        matchId: UUID,
        userId: UUID,
        scoreHome: Int,
        scoreAway: Int,
        status: MatchStatus,
        winnerId: UUID?
    ): Match {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")
        
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")
        
        val tournament = tournamentRepository.findById(match.tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")

        // Validar permisos según el rol
        when (user.role) {
            UserRole.admin -> {
                // Los admins pueden actualizar cualquier partido
            }
            UserRole.organizer -> {
                // Los organizadores solo pueden actualizar partidos de sus torneos
                if (tournament.organizerId != userId) {
                    throw SecurityException("Solo el organizador del torneo puede actualizar este partido")
                }
            }
            UserRole.referee -> {
                // Los árbitros solo pueden actualizar partidos donde estén asignados directamente
                if (match.refereeId != userId) {
                    throw SecurityException("No tienes permiso para arbitrar este partido. Debes estar asignado directamente al partido.")
                }
            }
            else -> {
                throw SecurityException("No tienes permiso para actualizar resultados de partidos")
            }
        }

        val updatedMatch = match.copy(
            scoreHome = scoreHome,
            scoreAway = scoreAway,
            status = status,
            winnerId = winnerId,
            finishedAt = if (status == MatchStatus.finished) Instant.now() else match.finishedAt,
            updatedAt = Instant.now()
        )

        val savedMatch = matchRepository.update(updatedMatch)
            ?: throw IllegalStateException("Error al guardar")

        if (savedMatch.groupId != null && savedMatch.status == MatchStatus.finished) {
            standingRepository.updateStandings(savedMatch.groupId)
        }

        return savedMatch
    }
}