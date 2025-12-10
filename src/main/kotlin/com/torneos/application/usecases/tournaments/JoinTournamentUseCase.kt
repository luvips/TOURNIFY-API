package com.torneos.application.usecases.tournaments

import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.services.TournamentWaitingQueueService
import java.util.UUID

class JoinTournamentUseCase(
    private val teamRepository: TeamRepository,
    private val tournamentRepository: TournamentRepository,
    private val registrationRepository: RegistrationRepository
) {
    
    data class JoinResult(
        val success: Boolean,
        val message: String,
        val isInWaitingQueue: Boolean = false,
        val queuePosition: Int? = null
    )
    
    suspend fun execute(userId: UUID, tournamentId: UUID, teamId: UUID): JoinResult {
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw IllegalArgumentException("Torneo no existe")

        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("Equipo no existe")

        if (team.captainId != userId) {
            throw SecurityException("Solo el capitán puede inscribir al equipo")
        }

        // Verificar si ya está inscrito
        val existing = registrationRepository.findByTournamentAndTeam(tournamentId, teamId)
        if (existing != null) {
            throw IllegalArgumentException("El equipo ya está inscrito en este torneo")
        }

        // Verificar si ya está en la cola de espera
        if (TournamentWaitingQueueService.isInQueue(tournamentId, teamId)) {
            val position = TournamentWaitingQueueService.getPosition(tournamentId, teamId)
            return JoinResult(
                success = false,
                message = "El equipo ya está en la lista de espera",
                isInWaitingQueue = true,
                queuePosition = position
            )
        }

        // Verificar si el torneo está lleno
        if (tournament.currentTeams >= tournament.maxTeams) {
            // Agregar a la cola de espera (uso de QUEUE)
            val enqueued = TournamentWaitingQueueService.enqueue(tournamentId, teamId, userId)
            if (enqueued) {
                val position = TournamentWaitingQueueService.getPosition(tournamentId, teamId)
                return JoinResult(
                    success = true,
                    message = "Torneo lleno. Equipo agregado a la lista de espera",
                    isInWaitingQueue = true,
                    queuePosition = position
                )
            } else {
                throw IllegalStateException("No se pudo agregar a la lista de espera")
            }
        }

        // Inscribir normalmente si hay espacio
        val registration = TeamRegistration(
            id = UUID.randomUUID(),
            tournamentId = tournamentId,
            teamId = teamId,
            groupId = null,
            status = if (tournament.requiresApproval) RegistrationStatus.pending else RegistrationStatus.approved,
            paymentStatus = PaymentStatus.unpaid,
            additionalInfo = null,
            seedNumber = null,
            approvedAt = null,
            approvedBy = null
        )

        registrationRepository.create(registration)
        
        return JoinResult(
            success = true,
            message = "Equipo inscrito exitosamente",
            isInWaitingQueue = false
        )
    }
}