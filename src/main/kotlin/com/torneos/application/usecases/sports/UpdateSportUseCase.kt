package com.torneos.application.usecases.sports

import com.torneos.domain.models.Sport
import com.torneos.domain.ports.SportRepository
import java.util.UUID

class UpdateSportUseCase(private val sportRepository: SportRepository) {
    suspend fun execute(id: UUID, sport: Sport): Sport {
        val sportToUpdate = sport.copy(id = id)

        return sportRepository.update(sportToUpdate)
            ?: throw NoSuchElementException("Deporte no encontrado")
    }
}