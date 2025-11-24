package com.torneos.domain.ports

import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.enums.PaymentStatus
import java.util.UUID

interface RegistrationRepository {
    // CRUD Básico
    suspend fun create(registration: TeamRegistration): TeamRegistration
    suspend fun findById(id: UUID): TeamRegistration?
    suspend fun findAll(): List<TeamRegistration>
    suspend fun update(registration: TeamRegistration): TeamRegistration?
    suspend fun delete(id: UUID): Boolean

    // Búsquedas Específicas
    suspend fun findByTournamentId(tournamentId: UUID): List<TeamRegistration>
    suspend fun findByTeamId(teamId: UUID): List<TeamRegistration>
    suspend fun findByTournamentAndTeam(tournamentId: UUID, teamId: UUID): TeamRegistration?

    // Gestión de Estado y Pagos
    suspend fun updateStatus(id: UUID, status: RegistrationStatus): Boolean
    suspend fun updatePaymentStatus(id: UUID, status: PaymentStatus): Boolean
}