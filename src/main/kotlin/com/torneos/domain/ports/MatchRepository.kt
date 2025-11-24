package com.torneos.domain.ports

import com.torneos.domain.models.Match
import com.torneos.domain.models.MatchResult
import java.util.UUID

interface MatchRepository {
    suspend fun create(match: Match): Match
    suspend fun findById(id: UUID): Match?
    suspend fun findByTournament(tournamentId: UUID): List<Match>
    suspend fun findByGroup(groupId: UUID): List<Match>

    suspend fun update(match: Match): Match?

    // Resultados detallados (Eventos)
    suspend fun addMatchEvent(result: MatchResult): MatchResult
    suspend fun getMatchEvents(matchId: UUID): List<MatchResult>
}