package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class GetTournamentDetailsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val fileStorage: FileStoragePort // Inyectar
) {
    suspend fun execute(id: UUID): Tournament {
        val tournament = tournamentRepository.findById(id)
            ?: throw NoSuchElementException("Torneo no encontrado")

        val signedUrl = tournament.imageUrl?.let { key ->
            fileStorage.getPresignedUrl(key)
        }

        return tournament.copy(imageUrl = signedUrl)
    }
}