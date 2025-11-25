package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.TournamentGroup
import com.torneos.domain.ports.TournamentGroupRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentGroupsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresTournamentGroupRepository : TournamentGroupRepository {

    private fun ResultRow.toTournamentGroup() = TournamentGroup(
        id = this[TournamentGroupsTable.id],
        tournamentId = this[TournamentGroupsTable.tournamentId],
        groupName = this[TournamentGroupsTable.groupName],
        displayOrder = this[TournamentGroupsTable.displayOrder],
        createdAt = this[TournamentGroupsTable.createdAt]
    )

    override suspend fun create(group: TournamentGroup): TournamentGroup = dbQuery {
        TournamentGroupsTable.insert {
            it[id] = group.id
            it[tournamentId] = group.tournamentId
            it[groupName] = group.groupName
            it[displayOrder] = group.displayOrder
        }
        group
    }

    override suspend fun findByTournament(tournamentId: UUID): List<TournamentGroup> = dbQuery {
        TournamentGroupsTable.selectAll()
            .where { TournamentGroupsTable.tournamentId eq tournamentId }
            .orderBy(TournamentGroupsTable.displayOrder to SortOrder.ASC)
            .map { it.toTournamentGroup() }
    }

    override suspend fun findById(id: UUID): TournamentGroup? = dbQuery {
        TournamentGroupsTable.selectAll().where { TournamentGroupsTable.id eq id }
            .map { it.toTournamentGroup() }
            .singleOrNull()
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TournamentGroupsTable.deleteWhere { TournamentGroupsTable.id eq id } > 0
    }
}
