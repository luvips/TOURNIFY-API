package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class StartTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(tournamentId: UUID, requesterId: UUID): Tournament {
        // 1. Buscar el torneo existente
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("El torneo no existe.")

        // 2. Validar permisos: Solo el organizador puede iniciar el torneo
        if (tournament.organizerId != requesterId) {
            throw SecurityException("No tienes permiso para iniciar este torneo. Solo el organizador puede hacerlo.")
        }

        // 3. Validar que el torneo esté en estado válido para iniciar
        if (tournament.status != "upcoming" && tournament.status != "registration") {
            throw IllegalStateException("El torneo no puede ser iniciado. Estado actual: ${tournament.status}")
        }

        // 4. Validar que haya al menos 2 equipos inscritos
        if (tournament.currentTeams < 2) {
            throw IllegalStateException("El torneo necesita al menos 2 equipos inscritos para poder iniciarse. Equipos actuales: ${tournament.currentTeams}")
        }

        // 5. Cambiar el estado del torneo a ongoing
        val updatedTournament = tournament.copy(status = "ongoing")

        // 6. Guardar cambios en el repositorio
        return tournamentRepository.update(updatedTournament)
            ?: throw IllegalStateException("Error al actualizar el estado del torneo.")
    }
}
