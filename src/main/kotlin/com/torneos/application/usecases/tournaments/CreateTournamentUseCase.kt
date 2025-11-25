package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.time.Instant

class CreateTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(tournament: Tournament): Tournament {
        if (tournament.startDate.isBefore(Instant.now())) {
            throw IllegalArgumentException("La fecha de inicio no puede ser en el pasado")
        }
        if (tournament.maxTeams < 2) {
            throw IllegalArgumentException("El torneo debe tener al menos 2 equipos")
        }
        return tournamentRepository.create(tournament)
    }
}