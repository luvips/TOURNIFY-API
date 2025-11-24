package com.torneos.domain.ports

import com.torneos.domain.models.Match
import java.util.UUID

interface MatchRepository {
    suspend fun create(match: Match): Match
    suspend fun findById(id: UUID): Match?
    suspend fun findByTournament(tournamentId: UUID): List<Match>
    suspend fun update(match: Match): Match?
    suspend fun delete(id: UUID): Boolean
}