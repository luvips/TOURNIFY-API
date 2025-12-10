package com.torneos.application.usecases.matches

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.UserRepository
import com.torneos.domain.services.MatchHistoryStack
import java.time.Instant
import java.util.UUID

/**
 * Caso de uso para deshacer el último resultado registrado en un partido.
 * Demuestra el uso de PILA (Stack) con operación POP.
 */
class UndoMatchResultUseCase(
    private val matchRepository: MatchRepository,
    private val standingRepository: StandingRepository,
    private val userRepository: UserRepository,
    private val tournamentRepository: TournamentRepository
) {

    data class UndoResult(
        val success: Boolean,
        val message: String,
        val restoredMatch: Match?,
        val previousState: Map<String, Any?>
    )

    suspend fun execute(matchId: UUID, userId: UUID): UndoResult {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")

        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        val tournament = tournamentRepository.findById(match.tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")

        // Validar permisos
        when (user.role) {
            UserRole.admin -> {
                // Los admins pueden deshacer cualquier partido
            }
            UserRole.organizer -> {
                if (tournament.organizerId != userId) {
                    throw SecurityException("Solo el organizador del torneo puede deshacer cambios")
                }
            }
            UserRole.referee -> {
                if (match.refereeId != userId) {
                    throw SecurityException("Solo puedes deshacer cambios en partidos donde eres árbitro")
                }
            }
            else -> {
                throw SecurityException("No tienes permiso para deshacer resultados")
            }
        }

        // Verificar si hay historial disponible
        if (!MatchHistoryStack.canUndo(matchId)) {
            return UndoResult(
                success = false,
                message = "No hay cambios anteriores para deshacer",
                restoredMatch = null,
                previousState = emptyMap()
            )
        }

        // DESHACER: Hacer POP de la pila para obtener el estado anterior
        val snapshot = MatchHistoryStack.pop(matchId)
            ?: return UndoResult(
                success = false,
                message = "Error al obtener el estado anterior",
                restoredMatch = null,
                previousState = emptyMap()
            )

        // Restaurar el estado anterior del partido
        val restoredMatch = match.copy(
            scoreHome = snapshot.scoreHome,
            scoreAway = snapshot.scoreAway,
            winnerId = snapshot.winnerId,
            status = MatchStatus.valueOf(snapshot.status),
            matchDataJson = snapshot.matchDataJson,
            finishedAt = snapshot.finishedAt?.let { Instant.parse(it) },
            updatedAt = Instant.now()
        )

        val savedMatch = matchRepository.update(restoredMatch)
            ?: throw IllegalStateException("Error al restaurar el estado del partido")

        // Actualizar standings si es necesario
        if (savedMatch.groupId != null) {
            standingRepository.updateStandings(savedMatch.groupId)
        }

        return UndoResult(
            success = true,
            message = "Resultado deshecho exitosamente",
            restoredMatch = savedMatch,
            previousState = mapOf(
                "scoreHome" to snapshot.scoreHome,
                "scoreAway" to snapshot.scoreAway,
                "winnerId" to snapshot.winnerId?.toString(),
                "status" to snapshot.status,
                "timestamp" to snapshot.timestamp
            )
        )
    }

    /**
     * Obtener el historial de cambios de un partido
     */
    fun getMatchHistory(matchId: UUID): List<Map<String, Any?>> {
        val history = MatchHistoryStack.getHistory(matchId)
        return history.map { snapshot ->
            mapOf(
                "scoreHome" to snapshot.scoreHome,
                "scoreAway" to snapshot.scoreAway,
                "winnerId" to snapshot.winnerId?.toString(),
                "status" to snapshot.status,
                "timestamp" to snapshot.timestamp,
                "updatedBy" to snapshot.updatedBy.toString()
            )
        }
    }

    /**
     * Verificar si se puede deshacer un partido
     */
    fun canUndo(matchId: UUID): Boolean {
        return MatchHistoryStack.canUndo(matchId)
    }
}
