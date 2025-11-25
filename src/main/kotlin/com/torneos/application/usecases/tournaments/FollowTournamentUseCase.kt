package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.TournamentFollower
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class FollowTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(userId: UUID, tournamentId: UUID) {
        val follower = TournamentFollower(userId, tournamentId)
        tournamentRepository.addFollower(follower)
    }
}