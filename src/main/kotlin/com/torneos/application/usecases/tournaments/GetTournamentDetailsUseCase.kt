package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class GetTournamentDetailsUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(id: UUID): Tournament {
        return tournamentRepository.findById(id)
            ?: throw NoSuchElementException("Torneo no encontrado")
    }
}