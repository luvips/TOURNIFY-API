package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object SportsTable : Table("sports") {
    val id = uuid("id")
    val name = varchar("name", 50)
<<<<<<< Updated upstream
    val category = varchar("category", 20)
=======
    val category = enumerationByName("category", 20, SportCategory::class)
>>>>>>> Stashed changes
    val icon = varchar("icon", 100).nullable()
    val defaultPlayersPerTeam = integer("default_players_per_team").nullable()
    val defaultMatchDuration = integer("default_match_duration").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}