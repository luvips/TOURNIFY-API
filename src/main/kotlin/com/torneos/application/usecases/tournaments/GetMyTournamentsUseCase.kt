package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.FileStoragePort // Importar
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class GetMyTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(organizerId: UUID): List<Tournament> {
        val tournaments = tournamentRepository.findByOrganizer(organizerId)

        return tournaments.map { tournament ->
            val signedUrl = tournament.imageUrl?.let { fileStorage.getPresignedUrl(it) }
            tournament.copy(imageUrl = signedUrl)
        }
    }
}