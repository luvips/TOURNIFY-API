package com.torneos.application.usecases.tournaments
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class DeleteTournamentUseCase(private val repository: TournamentRepository) {
    suspend fun execute(id: UUID, requesterId: UUID) {
        val tournament = repository.findById(id) ?: throw NoSuchElementException("Torneo no encontrado")
        if (tournament.organizerId != requesterId) throw SecurityException("Sin permiso")
        repository.delete(id)
    }
}