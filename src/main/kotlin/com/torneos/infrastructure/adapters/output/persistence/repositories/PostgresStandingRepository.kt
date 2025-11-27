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
            // 1. Obtener todos los partidos FINALIZADOS del grupo
            val matches = MatchesTable.selectAll()
                .where {
                    (MatchesTable.groupId eq groupId) and
                            (MatchesTable.status eq MatchStatus.finished)
                }
                .map { row ->
                    Triple(
                        row[MatchesTable.teamHomeId],
                        row[MatchesTable.teamAwayId],
                        Pair(row[MatchesTable.scoreHome] ?: 0, row[MatchesTable.scoreAway] ?: 0)
                    )
                }

            // 2. Mapa para acumular estadísticas por equipo
            val statsMap = mutableMapOf<UUID, TeamStats>()

            // 3. Procesar cada partido y sumar puntos
            matches.forEach { (homeId, awayId, scores) ->
                val (scoreHome, scoreAway) = scores

                if (homeId != null && awayId != null) {
                    val homeStats = statsMap.getOrPut(homeId) { TeamStats() }
                    val awayStats = statsMap.getOrPut(awayId) { TeamStats() }

                    // Partidos Jugados
                    homeStats.played++
                    awayStats.played++

                    // Goles
                    homeStats.gf += scoreHome
                    homeStats.ga += scoreAway
                    awayStats.gf += scoreAway
                    awayStats.ga += scoreHome

                    // Resultados (Victorias, Empates, Derrotas)
                    when {
                        scoreHome > scoreAway -> {
                            homeStats.won++
                            homeStats.pts += 3
                            awayStats.lost++
                        }
                        scoreAway > scoreHome -> {
                            awayStats.won++
                            awayStats.pts += 3
                            homeStats.lost++
                        }
                        else -> {
                            homeStats.drawn++
                            homeStats.pts += 1
                            awayStats.drawn++
                            awayStats.pts += 1
                        }
                    }
                }
            }

            // 4. Actualizar la base de datos
            // Primero eliminamos los registros actuales del grupo para re-insertarlos limpios
            GroupStandingsTable.deleteWhere { GroupStandingsTable.groupId eq groupId }

            // Insertamos las nuevas estadísticas calculadas
            statsMap.forEach { (teamId, stat) ->
                GroupStandingsTable.insert {
                    it[id] = UUID.randomUUID()
                    it[this.groupId] = groupId
                    it[this.teamId] = teamId
                    it[played] = stat.played
                    it[won] = stat.won
                    it[drawn] = stat.drawn
                    it[lost] = stat.lost
                    it[goalsFor] = stat.gf
                    it[goalsAgainst] = stat.ga
                    it[goalDifference] = stat.gf - stat.ga
                    it[points] = stat.pts
                    // La posición se calculará al hacer el GET con orderBy
                    it[position] = null
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}