package com.torneos.application.usecases.sports

import com.torneos.domain.models.Sport
import com.torneos.domain.ports.SportRepository

class GetSportsUseCase(private val sportRepository: SportRepository) {
    suspend fun execute(): List<Sport> {
        return sportRepository.findAll()
    }
}