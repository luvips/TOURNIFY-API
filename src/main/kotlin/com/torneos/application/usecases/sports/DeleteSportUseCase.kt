package com.torneos.application.usecases.sports

import com.torneos.domain.ports.SportRepository
import java.util.UUID

class DeleteSportUseCase(private val sportRepository: SportRepository) {
    suspend fun execute(id: UUID) {
        val deleted = sportRepository.delete(id)
        if (!deleted) {
            throw NoSuchElementException("No se pudo eliminar: Deporte no encontrado")
        }
    }
}