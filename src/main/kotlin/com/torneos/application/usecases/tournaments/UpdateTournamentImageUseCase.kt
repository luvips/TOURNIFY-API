package com.torneos.application.usecases.tournaments

import com.torneos.domain.ports.FileStoragePort
import com.torneos.domain.ports.TournamentRepository
import java.util.UUID

class UpdateTournamentImageUseCase(
    private val tournamentRepository: TournamentRepository,
    private val fileStorage: FileStoragePort
) {
    suspend fun execute(tournamentId: UUID, requesterId: UUID, fileName: String, fileBytes: ByteArray, contentType: String): String {
        // 1. Buscar el torneo
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")

        // 2. Validar permisos: Solo el organizador puede cambiar la imagen
        if (tournament.organizerId != requesterId) {
            throw SecurityException("Solo el organizador puede actualizar la imagen del torneo")
        }

        // 3. Subir archivo a S3 (Usa tu S3Service ya configurado con IAM Role)
        val objectKey = fileStorage.uploadFile(fileName, fileBytes, contentType)

        // 4. Actualizar el torneo con la nueva Key
        val updatedTournament = tournament.copy(imageUrl = objectKey)
        tournamentRepository.update(updatedTournament)

        // 5. Retornar la URL firmada para que el frontend la muestre de inmediato
        return fileStorage.getPresignedUrl(objectKey)
    }
}