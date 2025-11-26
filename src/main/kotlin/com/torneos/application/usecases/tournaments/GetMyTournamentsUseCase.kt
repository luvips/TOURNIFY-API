package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

/**
 * Obtiene los torneos creados por un organizador
 */
class GetMyTournamentsUseCase(
    private val tournamentRepository: TournamentRepository
) {
    suspend fun execute(organizerId: UUID): List<Tournament> {
        return tournamentRepository.findByOrganizer(organizerId)
    }
}
