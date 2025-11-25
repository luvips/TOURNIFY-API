package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import com.torneos.infrastructure.adapters.output.persistence.tables.*
object TournamentFollowersTable : Table("tournament_followers") {
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val tournamentId = uuid("tournament_id").references(TournamentsTable.id, onDelete = ReferenceOption.CASCADE)
    val followedAt = timestamp("followed_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(userId, tournamentId)
}