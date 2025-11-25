package com.torneos.application.usecases.tournaments

import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.ports.TeamRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.RegistrationRepository
import java.util.UUID

class JoinTournamentUseCase(
    private val teamRepository: TeamRepository,
    private val tournamentRepository: TournamentRepository,
    private val registrationRepository: RegistrationRepository
) {
    suspend fun execute(userId: UUID, tournamentId: UUID, teamId: UUID) {
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
    }
}