package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.GroupStanding
import com.torneos.domain.ports.StandingRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.GroupStandingsTable
import org.jetbrains.exposed.sql.*
import java.util.UUID

class PostgresStandingRepository : StandingRepository {

    private fun ResultRow.toGroupStanding() = GroupStanding(
        id = this[GroupStandingsTable.id],
        groupId = this[GroupStandingsTable.groupId],
        teamId = this[GroupStandingsTable.teamId],
        played = this[GroupStandingsTable.played],
        won = this[GroupStandingsTable.won],
        drawn = this[GroupStandingsTable.drawn],
        lost = this[GroupStandingsTable.lost],
        goalsFor = this[GroupStandingsTable.goalsFor],
        goalsAgainst = this[GroupStandingsTable.goalsAgainst],
        goalDifference = this[GroupStandingsTable.goalDifference],
        points = this[GroupStandingsTable.points],
        position = this[GroupStandingsTable.position]
    )

    override suspend fun getStandingsByGroup(groupId: UUID): List<GroupStanding> = dbQuery {
        GroupStandingsTable.selectAll()
            .where { GroupStandingsTable.groupId eq groupId }
            .orderBy(
                Pair(GroupStandingsTable.points, SortOrder.DESC),
                Pair(GroupStandingsTable.goalDifference, SortOrder.DESC)
            )
            .map { it.toGroupStanding() }
    }

    override suspend fun updateStandings(groupId: UUID): Boolean {
        // TODO: Aquí iría la lógica compleja de recalcular puntos sumando los partidos
        // Por ahora retornamos true para cumplir la interfaz
        return true
    }
}