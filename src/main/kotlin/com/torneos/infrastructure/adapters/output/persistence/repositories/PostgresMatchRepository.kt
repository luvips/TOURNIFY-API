package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.GroupStanding
import com.torneos.domain.models.Match
import com.torneos.domain.models.MatchResult
import com.torneos.domain.ports.MatchRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.GroupStandingsTable
import com.torneos.infrastructure.adapters.output.persistence.tables.MatchResultsTable
import com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresMatchRepository : MatchRepository {

    private fun ResultRow.toMatch() = Match(
        id = this[MatchesTable.id],
        tournamentId = this[MatchesTable.tournamentId],
        groupId = this[MatchesTable.groupId],
        matchNumber = this[MatchesTable.matchNumber],
        roundName = this[MatchesTable.roundName],
        roundNumber = this[MatchesTable.roundNumber],
        teamHomeId = this[MatchesTable.teamHomeId],
        teamAwayId = this[MatchesTable.teamAwayId],
        scheduledDate = this[MatchesTable.scheduledDate],
        location = this[MatchesTable.location],
        refereeId = this[MatchesTable.refereeId],
        status = this[MatchesTable.status],
        scoreHome = this[MatchesTable.scoreHome],
        scoreAway = this[MatchesTable.scoreAway],
        winnerId = this[MatchesTable.winnerId],
        matchDataJson = this[MatchesTable.matchData],
        notes = this[MatchesTable.notes],
        startedAt = this[MatchesTable.startedAt],
        finishedAt = this[MatchesTable.finishedAt],
        createdAt = this[MatchesTable.createdAt],
        updatedAt = this[MatchesTable.updatedAt]
    )

    override suspend fun create(match: Match): Match = dbQuery {
        MatchesTable.insert {
            it[id] = match.id
            it[tournamentId] = match.tournamentId
            it[groupId] = match.groupId
            it[teamHomeId] = match.teamHomeId
            it[teamAwayId] = match.teamAwayId
            it[scheduledDate] = match.scheduledDate
            it[status] = match.status
            // ... resto
        }
        match
    }

    override suspend fun findById(id: UUID): Match? = dbQuery {
        MatchesTable.selectAll().where { MatchesTable.id eq id }
            .map { it.toMatch() }
            .singleOrNull()
    }

    override suspend fun findByTournament(tournamentId: UUID): List<Match> = dbQuery {
        MatchesTable.selectAll().where { MatchesTable.tournamentId eq tournamentId }
            .orderBy(MatchesTable.roundNumber to SortOrder.ASC, MatchesTable.matchNumber to SortOrder.ASC)
            .map { it.toMatch() }
    }
    
    override suspend fun findByGroup(groupId: UUID): List<Match> = dbQuery {
        MatchesTable.selectAll().where { MatchesTable.groupId eq groupId }
            .map { it.toMatch() }
    }

    override suspend fun update(match: Match): Match? = dbQuery {
        val rows = MatchesTable.update({ MatchesTable.id eq match.id }) {
            it[scoreHome] = match.scoreHome
            it[scoreAway] = match.scoreAway
            it[status] = match.status
            it[winnerId] = match.winnerId
            it[finishedAt] = match.finishedAt
        }
        if (rows > 0) match else null
    }

    // --- EVENTS (Results) ---
    override suspend fun addMatchEvent(result: MatchResult): MatchResult = dbQuery {
        MatchResultsTable.insert {
            it[id] = result.id
            it[matchId] = result.matchId
            it[teamId] = result.teamId
            it[playerId] = result.playerId
            it[eventType] = result.eventType
            it[eventTime] = result.eventTime
        }
        result
    }

    override suspend fun getMatchEvents(matchId: UUID): List<MatchResult> = dbQuery {
        MatchResultsTable.selectAll()
            .where { MatchResultsTable.matchId eq matchId }
            .orderBy(MatchResultsTable.eventTime to SortOrder.ASC)
            .map { it.toMatchResult() }
    }

    private fun ResultRow.toMatchResult() = MatchResult(
        id = this[MatchResultsTable.id],
        matchId = this[MatchResultsTable.matchId],
        teamId = this[MatchResultsTable.teamId],
        playerId = this[MatchResultsTable.playerId],
        eventType = this[MatchResultsTable.eventType],
        eventTime = this[MatchResultsTable.eventTime],
        eventPeriod = this[MatchResultsTable.eventPeriod],
        eventDataJson = this[MatchResultsTable.eventData],
        notes = this[MatchResultsTable.notes],
        createdAt = this[MatchResultsTable.createdAt]
    )

    // --- STANDINGS ---
    override suspend fun getStandings(tournamentId: UUID): List<GroupStanding> = dbQuery {
        // En un caso real, haríamos un JOIN con TournamentGroups para filtrar por torneo
        // Aquí simplificado: devuelve todo lo que haya en la tabla standings
        GroupStandingsTable.selectAll()
            .orderBy(GroupStandingsTable.points to SortOrder.DESC, GroupStandingsTable.goalDifference to SortOrder.DESC)
            .map {
                GroupStanding(
                    id = it[GroupStandingsTable.id],
                    groupId = it[GroupStandingsTable.groupId],
                    teamId = it[GroupStandingsTable.teamId],
                    played = it[GroupStandingsTable.played],
                    won = it[GroupStandingsTable.won],
                    drawn = it[GroupStandingsTable.drawn],
                    lost = it[GroupStandingsTable.lost],
                    goalsFor = it[GroupStandingsTable.goalsFor],
                    goalsAgainst = it[GroupStandingsTable.goalsAgainst],
                    goalDifference = it[GroupStandingsTable.goalDifference],
                    points = it[GroupStandingsTable.points],
                    position = it[GroupStandingsTable.position]
                )
            }
    }
}