package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

/**
 * Obtiene los torneos que un usuario está siguiendo
 */
class GetFollowedTournamentsUseCase(
    private val tournamentRepository: TournamentRepository
) {
    suspend fun execute(userId: UUID): List<Tournament> {
        return tournamentRepository.findFollowedByUser(userId)
    }
}
