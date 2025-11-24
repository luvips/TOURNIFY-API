package com.torneos.domain.ports

import com.torneos.domain.models.GroupStanding
import java.util.UUID

interface StandingRepository {
    // Obtener la tabla de un grupo
    suspend fun getStandingsByGroup(groupId: UUID): List<GroupStanding>

    // Actualizar/Recalcular tabla (llamado tras finalizar un partido)
    suspend fun updateStandings(groupId: UUID): Boolean
}