package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository

class GetTournamentsUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(page: Int, size: Int): List<Tournament> {
        return tournamentRepository.findAll(page, size)
    }
}