package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.FileStoragePort // Importar
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class GetFollowedTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val fileStorage: FileStoragePort // Inyectar
) {
    suspend fun execute(userId: UUID): List<Tournament> {
        val tournaments = tournamentRepository.findFollowedByUser(userId)

        return tournaments.map { tournament ->
            val signedUrl = tournament.imageUrl?.let { fileStorage.getPresignedUrl(it) }
            tournament.copy(imageUrl = signedUrl)
        }
    }
}