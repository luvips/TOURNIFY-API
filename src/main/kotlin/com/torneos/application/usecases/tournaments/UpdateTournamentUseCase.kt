package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class UpdateTournamentUseCase(private val tournamentRepository: TournamentRepository) {
    suspend fun execute(id: UUID, tournament: Tournament, requesterId: UUID): Tournament {
        // 1. Buscar el torneo existente
        val existingTournament = tournamentRepository.findById(id)
            ?: throw NoSuchElementException("El torneo no existe.")

        // 2. Validar permisos: Solo el organizador puede editarlo
        if (existingTournament.organizerId != requesterId) {
            throw SecurityException("No tienes permiso para editar este torneo (Solo el organizador).")
        }

        // 3. Mantener el ID original y el Organizador original, actualizando el resto
        // Nota: Reutilizamos el objeto 'tournament' que viene del JSON, pero forzamos el ID correcto
        val tournamentToUpdate = tournament.copy(
            id = id,
            organizerId = existingTournament.organizerId, // No cambiamos el dueño
            sportId = existingTournament.sportId,         // Generalmente no se cambia el deporte
            sport = existingTournament.sport
        )

        // 4. Guardar cambios
        return tournamentRepository.update(tournamentToUpdate)
            ?: throw IllegalStateException("Error al actualizar el torneo.")
    }
}