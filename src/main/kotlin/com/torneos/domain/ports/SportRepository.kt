package com.torneos.domain.ports

import com.torneos.domain.models.Sport
import java.util.UUID

interface SportRepository {
    suspend fun findAll(): List<Sport>
    suspend fun findById(id: UUID): Sport?
    suspend fun create(sport: Sport): Sport
    suspend fun toggleActive(id: UUID, isActive: Boolean): Boolean
    suspend fun update(sport: Sport): Sport?
    suspend fun delete(id: UUID): Boolean
}