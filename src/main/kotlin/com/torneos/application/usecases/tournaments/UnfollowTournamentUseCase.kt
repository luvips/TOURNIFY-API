package com.torneos.application.usecases.tournaments

import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class UnfollowTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(userId: UUID, tournamentId: UUID) {
        tournamentRepository.removeFollower(userId, tournamentId)
    }
}