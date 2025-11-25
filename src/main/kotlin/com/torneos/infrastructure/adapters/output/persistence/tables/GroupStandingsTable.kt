package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object GroupStandingsTable : Table("group_standings") {
    val id = uuid("id")
    val groupId = uuid("group_id").references(TournamentGroupsTable.id, onDelete = ReferenceOption.CASCADE)
    val teamId = uuid("team_id").references(TeamsTable.id, onDelete = ReferenceOption.CASCADE)
    
    val played = integer("played").default(0)
    val won = integer("won").default(0)
    val drawn = integer("drawn").default(0)
    val lost = integer("lost").default(0)
    val goalsFor = integer("goals_for").default(0)
    val goalsAgainst = integer("goals_against").default(0)
    val goalDifference = integer("goal_difference").default(0)
    val points = integer("points").default(0)
    
    val position = integer("position").nullable()
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}