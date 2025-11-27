package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object TournamentsTable : Table("tournaments") {
    val id = uuid("id")
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val sport_id = uuid("sport_id").references(SportsTable.id)
    val sport = varchar("sport", 100).nullable()
    val sport_sub_type = varchar("sport_sub_type", 50).nullable()
    val organizer_id = uuid("organizer_id").references(UsersTable.id)
    val tournament_type = varchar("tournament_type", 50)
    val category = varchar("category", 50).nullable()
    val elimination_mode = varchar("elimination_mode", 50).nullable()
    val location = varchar("location", 200).nullable()
    val start_date = timestamp("start_date")
    val end_date = timestamp("end_date").nullable()
    val registration_deadline = timestamp("registration_deadline").nullable()
    val max_teams = integer("max_teams")
    val current_teams = integer("current_teams").default(0)
    val registration_fee = decimal("registration_fee", 10, 2)
    val prize_pool = varchar("prize_pool", 100).nullable()
    val is_private = bool("is_private").default(false)
    val requires_approval = bool("requires_approval").default(false)
    val access_code = varchar("access_code", 50).nullable()
    val has_group_stage = bool("has_group_stage").default(false)
    val number_of_groups = integer("number_of_groups").nullable()
    val teams_per_group = integer("teams_per_group").nullable()
    val teams_advance_per_group = integer("teams_advance_per_group").nullable()
    val sport_settings = text("sport_settings").default("{}")
    val allow_ties = bool("allow_ties").default(false)
    val points_for_win = integer("points_for_win").default(3)
    val points_for_draw = integer("points_for_draw").default(1)
    val points_for_loss = integer("points_for_loss").default(0)
    val rules_text = text("rules_text").nullable()
    val image_url = varchar("image_url", 255).nullable()
    val status = varchar("status", 20).default("upcoming")
    val created_at = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updated_at = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}