package com.torneos.application.usecases.sports

import com.torneos.domain.models.Sport
import com.torneos.domain.ports.SportRepository

class CreateSportUseCase(private val sportRepository: SportRepository) {
    suspend fun execute(sport: Sport): Sport {
        return sportRepository.create(sport)
    }
}