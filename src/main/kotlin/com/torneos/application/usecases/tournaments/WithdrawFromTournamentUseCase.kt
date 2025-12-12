package com.torneos.application.usecases.tournaments

import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.services.TournamentWaitingQueueService
import java.util.UUID

class WithdrawFromTournamentUseCase(
    private val registrationRepository: RegistrationRepository,
    private val tournamentRepository: TournamentRepository
) {

    data class WithdrawResult(
        val success: Boolean,
        val message: String,
        val nextTeamProcessed: Boolean = false,
        val nextTeamId: UUID? = null
    )

    suspend fun execute(tournamentId: UUID, teamId: UUID, userId: UUID): WithdrawResult {
        val tournament = tournamentRepository.findById(tournamentId)
            ?: throw IllegalArgumentException("Torneo no existe")

        val registration = registrationRepository.findByTournamentAndTeam(tournamentId, teamId)
            ?: throw IllegalArgumentException("El equipo no está inscrito en este torneo")

        registrationRepository.delete(registration.id)

        val nextInQueue = TournamentWaitingQueueService.dequeue(tournamentId)

        if (nextInQueue != null) {
            val newRegistration = TeamRegistration(
                id = UUID.randomUUID(),
                tournamentId = tournamentId,
                teamId = nextInQueue.teamId,
                groupId = null,
                status = if (tournament.requiresApproval) RegistrationStatus.pending else RegistrationStatus.approved,
                paymentStatus = PaymentStatus.unpaid,
                additionalInfo = null,
                seedNumber = null,
                approvedAt = null,
                approvedBy = null
            )

            registrationRepository.create(newRegistration)

            return WithdrawResult(
                success = true,
                message = "Equipo retirado. Siguiente equipo en cola inscrito automáticamente",
                nextTeamProcessed = true,
                nextTeamId = nextInQueue.teamId
            )
        }

        return WithdrawResult(
            success = true,
            message = "Equipo retirado exitosamente. No hay equipos en cola de espera",
            nextTeamProcessed = false
        )
    }
}
