package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.timestamp

object TournamentGroupsTable : Table("tournament_groups") {
    val id = uuid("id")
    val tournamentId = uuid("tournament_id").references(TournamentsTable.id, onDelete = ReferenceOption.CASCADE)
    val groupName = varchar("group_name", 50)
    val displayOrder = integer("display_order")
    val createdAt = timestamp("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}