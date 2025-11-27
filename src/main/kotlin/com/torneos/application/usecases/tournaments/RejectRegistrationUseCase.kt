package com.torneos.application.usecases.tournaments

import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.ports.RegistrationRepository
import java.util.UUID

class RejectRegistrationUseCase(
    private val registrationRepository: RegistrationRepository
) {
    suspend fun execute(tournamentId: UUID, registrationId: UUID, reason: String?) {
        // Verificar que la registración existe
        val registration = registrationRepository.findById(registrationId)
            ?: throw NoSuchElementException("Registración no encontrada")
        
        // Verificar que pertenece al torneo
        if (registration.tournamentId != tournamentId) {
            throw IllegalArgumentException("La registración no pertenece a este torneo")
        }
        
        // Verificar que está pendiente
        if (registration.status != RegistrationStatus.pending) {
            throw IllegalStateException("Solo se pueden rechazar registraciones pendientes")
        }
        
        // Actualizar estado
        val updated = registration.copy(
            status = RegistrationStatus.rejected,
            additionalInfo = reason
        )
        
        registrationRepository.update(updated)
    }
}
