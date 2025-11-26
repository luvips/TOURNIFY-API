package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.SportRepository
import com.torneos.domain.ports.TournamentRepository

class CreateTournamentUseCase(
    private val tournamentRepository: TournamentRepository,
    private val sportRepository: SportRepository
) {
    suspend fun execute(tournament: Tournament): Tournament {
        // 1. Validar que el deporte existe y obtener su nombre
        val sport = sportRepository.findById(tournament.sportId)
            ?: throw IllegalArgumentException("El sportId '${tournament.sportId}' no existe.")

        // 2. Crear el objeto final con el nombre del deporte
        val tournamentWithSportName = tournament.copy(sport = sport.name)

        // 3. Guardar en la base de datos
        return tournamentRepository.create(tournamentWithSportName)
    }
}