package com.torneos.domain.ports

import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.enums.RegistrationStatus
import java.util.UUID

interface TeamRepository {
    // Equipos
    suspend fun create(team: Team): Team
    suspend fun findById(id: UUID): Team?
    suspend fun findByCaptain(captainId: UUID): List<Team>
    suspend fun update(team: Team): Team?
    suspend fun delete(id: UUID): Boolean


    // Miembros del Equipo
    suspend fun addMember(member: TeamMember): TeamMember
    suspend fun removeMember(memberId: UUID): Boolean
    suspend fun getMembers(teamId: UUID): List<TeamMember>

    // Inscripciones (Registrations)
    suspend fun registerToTournament(registration: TeamRegistration): TeamRegistration
    suspend fun getTournamentRegistrations(tournamentId: UUID): List<TeamRegistration>
    suspend fun updateRegistrationStatus(registrationId: UUID, status: RegistrationStatus): Boolean
}