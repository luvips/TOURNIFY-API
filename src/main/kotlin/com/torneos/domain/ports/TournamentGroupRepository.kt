package com.torneos.domain.ports

import com.torneos.domain.models.TournamentGroup
import java.util.UUID

interface TournamentGroupRepository {
    suspend fun create(group: TournamentGroup): TournamentGroup
    suspend fun findByTournament(tournamentId: UUID): List<TournamentGroup>
    suspend fun findById(id: UUID): TournamentGroup?
    suspend fun delete(id: UUID): Boolean
}