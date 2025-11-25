package com.torneos.infrastructure.adapters.output.persistence.tables

import com.torneos.domain.enums.SportCategory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object SportsTable : Table("sports") {
    val id = uuid("id")
    val name = varchar("name", 50)
    val category = postgresEnumeration("category", "sport_category", SportCategory::class.java)
    val icon = varchar("icon", 100).nullable()
    val defaultPlayersPerTeam = integer("default_players_per_team").nullable()
    val defaultMatchDuration = integer("default_match_duration").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}