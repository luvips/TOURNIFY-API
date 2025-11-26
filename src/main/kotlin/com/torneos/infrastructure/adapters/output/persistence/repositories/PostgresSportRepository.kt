package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.enums.SportCategory
import com.torneos.domain.models.Sport
import com.torneos.domain.ports.SportRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.SportsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresSportRepository : SportRepository {

    private fun ResultRow.toSport() = Sport(
        id = this[SportsTable.id],
        name = this[SportsTable.name],
        category = SportCategory.valueOf(this[SportsTable.category]),
        icon = this[SportsTable.icon],
        defaultPlayersPerTeam = this[SportsTable.defaultPlayersPerTeam],
        defaultMatchDuration = this[SportsTable.defaultMatchDuration],
        isActive = this[SportsTable.isActive],
        createdAt = this[SportsTable.createdAt]
    )

    override suspend fun findAll(): List<Sport> = dbQuery {
        SportsTable.selectAll().where { SportsTable.isActive eq true }
            .map { it.toSport() }
    }

    override suspend fun findById(id: UUID): Sport? = dbQuery {
        SportsTable.selectAll().where { SportsTable.id eq id }
            .map { it.toSport() }
            .singleOrNull()
    }

    override suspend fun create(sport: Sport): Sport = dbQuery {
        SportsTable.insert {
            it[id] = sport.id
            it[name] = sport.name
            it[category] = sport.category.name
            it[icon] = sport.icon
            it[defaultPlayersPerTeam] = sport.defaultPlayersPerTeam
            it[defaultMatchDuration] = sport.defaultMatchDuration
        }
        sport
    }
    
    override suspend fun toggleActive(id: UUID, isActive: Boolean): Boolean = dbQuery {
        SportsTable.update({ SportsTable.id eq id }) {
            it[SportsTable.isActive] = isActive
        } > 0
    }
    override suspend fun update(sport: Sport): Sport? = dbQuery {
        val rows = SportsTable.update({ SportsTable.id eq sport.id }) {
            it[name] = sport.name
            it[category] = sport.category.name
            it[defaultPlayersPerTeam] = sport.defaultPlayersPerTeam
            it[defaultMatchDuration] = sport.defaultMatchDuration
            // No actualizamos 'icon' aquí si se maneja por separado, o agrégalo si quieres
        }
        if (rows > 0) sport else null
    }

    // 👇 IMPLEMENTAR DELETE
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        SportsTable.deleteWhere { SportsTable.id eq id } > 0
    }
}