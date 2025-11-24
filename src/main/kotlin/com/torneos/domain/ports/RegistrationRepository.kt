package com.torneos.domain.ports

import com.torneos.domain.models.Registration
import com.torneos.domain.enums.RegistrationStatus
import java.util.UUID

interface RegistrationRepository {
    suspend fun register(registration: Registration): Registration
    suspend fun findByTournament(tournamentId: UUID): List<Registration>
    suspend fun updateStatus(id: UUID, status: RegistrationStatus): Boolean
}