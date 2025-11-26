package com.torneos.application.usecases.matches

import com.torneos.domain.ports.MatchRepository
import java.util.UUID

class DeleteMatchUseCase(private val matchRepository: MatchRepository) {
    suspend fun execute(matchId: UUID) {
        val deleted = matchRepository.delete(matchId)
        if (!deleted) {
            throw NoSuchElementException("El partido no existe o no se pudo eliminar")
        }
    }
}