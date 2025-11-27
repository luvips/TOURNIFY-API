package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class FinishTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(tournamentId: UUID, requesterId: UUID): Tournament {
        // 1. Buscar el torneo existente
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("El torneo no existe.")

        // 2. Validar permisos: Solo el organizador puede finalizar el torneo
        if (tournament.organizerId != requesterId) {
            throw SecurityException("No tienes permiso para finalizar este torneo. Solo el organizador puede hacerlo.")
        }

        // 3. Validar que el torneo esté en curso
        if (tournament.status != "ongoing") {
            throw IllegalStateException("Solo se pueden finalizar torneos que estén en curso. Estado actual: ${tournament.status}")
        }

        // 4. Cambiar el estado del torneo a finished
        val updatedTournament = tournament.copy(status = "finished")

        // 5. Guardar cambios en el repositorio
        return tournamentRepository.update(updatedTournament)
            ?: throw IllegalStateException("Error al actualizar el estado del torneo.")
    }
}
