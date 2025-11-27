package com.torneos.application.usecases.tournaments

import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TournamentRepository
import java.time.Instant
import java.util.UUID

class ApproveRegistrationUseCase(
    private val registrationRepository: RegistrationRepository,
    private val tournamentRepository: TournamentRepository
) {
    suspend fun execute(tournamentId: UUID, registrationId: UUID, approverId: UUID) {
        // Verificar que la registración existe
        val registration = registrationRepository.findById(registrationId)
            ?: throw NoSuchElementException("Registración no encontrada")
        
        // Verificar que pertenece al torneo
        if (registration.tournamentId != tournamentId) {
            throw IllegalArgumentException("La registración no pertenece a este torneo")
        }
        
        // Verificar que está pendiente
        if (registration.status != RegistrationStatus.pending) {
            throw IllegalStateException("Solo se pueden aprobar registraciones pendientes")
        }
        
        // Actualizar estado
        val updated = registration.copy(
            status = RegistrationStatus.approved,
            approvedAt = Instant.now(),
            approvedBy = approverId
        )
        
        registrationRepository.update(updated)
        
        // Incrementar contador de equipos en el torneo
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")
        
        val updatedTournament = tournament.copy(
            currentTeams = tournament.currentTeams + 1
        )
        
        tournamentRepository.update(updatedTournament)
    }
}
