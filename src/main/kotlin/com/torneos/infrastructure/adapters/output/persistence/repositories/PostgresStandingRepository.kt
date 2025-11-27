package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.GroupStanding
import com.torneos.domain.ports.StandingRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.GroupStandingsTable
import com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresStandingRepository : StandingRepository {

    // Mapper de Fila de BD a Modelo de Dominio
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
        position = this[GroupStandingsTable.position],
        updatedAt = this[GroupStandingsTable.updatedAt] // Asegúrate de tener este campo en el modelo o elimínalo si no lo usas
    )

    // Clase auxiliar interna para acumular estadísticas antes de guardar
    private data class TeamStats(
        var played: Int = 0,
        var won: Int = 0,
        var drawn: Int = 0,
        var lost: Int = 0,
        var gf: Int = 0,
        var ga: Int = 0,
        var pts: Int = 0
    )

    override suspend fun getStandingsByGroup(groupId: UUID): List<GroupStanding> = dbQuery {
        GroupStandingsTable.selectAll()
            .where { GroupStandingsTable.groupId eq groupId }
            .orderBy(
                Pair(GroupStandingsTable.points, SortOrder.DESC),
                Pair(GroupStandingsTable.goalDifference, SortOrder.DESC),
                Pair(GroupStandingsTable.goalsFor, SortOrder.DESC)
            )
            .mapIndexed { index, row ->
                // Asignamos la posición dinámicamente según el orden
                row.toGroupStanding().copy(position = index + 1)
            }
    }

    override suspend fun updateStandings(groupId: UUID): Boolean = dbQuery {
        try {
            // 1. Obtener partidos finalizados
            val matches = com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.selectAll()
                .where {
                    (com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.groupId eq groupId) and
                            (com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.status eq com.torneos.domain.enums.MatchStatus.finished)
                }

            // 2. Estructura para sumar puntos en memoria
            data class Stats(var pts: Int = 0, var gf: Int = 0, var ga: Int = 0, var w: Int = 0, var d: Int = 0, var l: Int = 0, var p: Int = 0)
            val teamStats = mutableMapOf<UUID, Stats>()

            // 3. Iterar partidos y sumar
            matches.forEach { row ->
                val home = row[com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.teamHomeId]
                val away = row[com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.teamAwayId]
                val sHome = row[com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.scoreHome] ?: 0
                val sAway = row[com.torneos.infrastructure.adapters.output.persistence.tables.MatchesTable.scoreAway] ?: 0

                if (home != null && away != null) {
                    val th = teamStats.getOrPut(home) { Stats() }
                    val ta = teamStats.getOrPut(away) { Stats() }

                    th.p++; ta.p++
                    th.gf += sHome; th.ga += sAway
                    ta.gf += sAway; ta.ga += sHome

                    when {
                        sHome > sAway -> { th.pts += 3; th.w++; ta.l++ }
                        sAway > sHome -> { ta.pts += 3; ta.w++; th.l++ }
                        else -> { th.pts += 1; th.d++; ta.pts += 1; ta.d++ }
                    }
                }
            }

            // 4. Borrar tabla actual e insertar nuevos valores
            GroupStandingsTable.deleteWhere { GroupStandingsTable.groupId eq groupId }

            teamStats.forEach { (tId, s) ->
                GroupStandingsTable.insert {
                    it[id] = UUID.randomUUID()
                    it[GroupStandingsTable.groupId] = groupId
                    it[teamId] = tId
                    it[played] = s.p
                    it[won] = s.w
                    it[drawn] = s.d
                    it[lost] = s.l
                    it[goalsFor] = s.gf
                    it[goalsAgainst] = s.ga
                    it[goalDifference] = s.gf - s.ga
                    it[points] = s.pts
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}