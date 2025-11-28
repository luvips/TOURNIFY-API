package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.Tournament
import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.UserRepository
import java.util.UUID

data class TournamentWithOrganizerInfo(
    val tournament: Tournament,
    val organizerUsername: String?
)

class GetTournamentDetailsUseCase(
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(id: UUID): TournamentWithOrganizerInfo {
        val tournament = tournamentRepository.findById(id)
            ?: throw NoSuchElementException("Torneo no encontrado")

        val signedUrl = tournament.imageUrl?.let { key ->
            fileStorage.getPresignedUrl(key)
        }

        val organizer = userRepository.findById(tournament.organizerId)
        val organizerUsername = organizer?.username

        return TournamentWithOrganizerInfo(
            tournament = tournament.copy(imageUrl = signedUrl),
            organizerUsername = organizerUsername
        )
    }
}