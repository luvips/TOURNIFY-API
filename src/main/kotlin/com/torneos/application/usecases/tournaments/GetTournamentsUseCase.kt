package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.TournamentRepository

class GetTournamentsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val fileStorage: FileStoragePort // 2. Inyectar servicio de archivos
) {
    suspend fun execute(page: Int, size: Int): List<Tournament> {
        val tournaments = tournamentRepository.findAll(page, size)

        // 3. "Mapear" la lista
        return tournaments.map { tournament ->
            val signedUrl = tournament.imageUrl?.let { key ->
                fileStorage.getPresignedUrl(key)
            }
            // Devolvemos el torneo con la URL lista para el frontend
            tournament.copy(imageUrl = signedUrl)
        }
    }
}