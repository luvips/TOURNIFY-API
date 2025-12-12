package com.torneos.infrastructure.adapters.output.persistence.tables

import com.torneos.domain.enums.MatchStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MatchesTable : Table("matches") {
    val id = uuid("id")
    val tournamentId = uuid("tournament_id").references(TournamentsTable.id, onDelete = ReferenceOption.CASCADE)
    val groupId = uuid("group_id").references(TournamentGroupsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    
    val matchNumber = integer("match_number").nullable()
    val roundName = varchar("round_name", 50).nullable()
    val roundNumber = integer("round_number").nullable()
    
    val teamHomeId = uuid("team_home_id").references(TeamsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val teamAwayId = uuid("team_away_id").references(TeamsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    
    val scheduledDate = timestamp("scheduled_date").nullable()
    val location = varchar("location", 200).nullable()
    val refereeId = uuid("referee_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    val status = enumerationByName("status", 20, MatchStatus::class).default(MatchStatus.scheduled)
    val scoreHome = integer("score_home").nullable()
    val scoreAway = integer("score_away").nullable()
    val winnerId = uuid("winner_id").references(TeamsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    
    val matchData = text("match_data").default("{}")
    val notes = text("notes").nullable()
    
    val startedAt = timestamp("started_at").nullable()
    val finishedAt = timestamp("finished_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
    

    override val primaryKey = PrimaryKey(id)
}

object MatchResultsTable : Table("match_results") {
    val id = uuid("id")
    val matchId = uuid("match_id").references(MatchesTable.id, onDelete = ReferenceOption.CASCADE)
    val teamId = uuid("team_id").references(TeamsTable.id, onDelete = ReferenceOption.CASCADE)
    val playerId = uuid("player_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    
    val eventType = varchar("event_type", 50)
    val eventTime = integer("event_time").nullable()
    val eventPeriod = varchar("event_period", 20).nullable()
    
    val eventData = text("event_data").default("{}")
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}