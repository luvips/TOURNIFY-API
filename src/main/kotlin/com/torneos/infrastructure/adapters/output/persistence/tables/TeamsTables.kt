package com.torneos.infrastructure.adapters.output.persistence.tables

import com.torneos.domain.enums.MemberRole
import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.enums.RegistrationStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object TeamsTable : Table("teams") {
    val id = uuid("id")
    val captainId = uuid("captain_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val name = varchar("name", 100)
    val shortName = varchar("short_name", 20).nullable()
    val logoUrl = varchar("logo_url", 255).nullable()
    val description = text("description").nullable()
    val contactEmail = varchar("contact_email", 100).nullable()
    val contactPhone = varchar("contact_phone", 20).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}

object TeamMembersTable : Table("team_members") {
    val id = uuid("id")
    val teamId = uuid("team_id").references(TeamsTable.id, onDelete = ReferenceOption.CASCADE)
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    val memberName = varchar("member_name", 100).nullable()
    val memberEmail = varchar("member_email", 100).nullable()
    val memberPhone = varchar("member_phone", 20).nullable()

    val role = enumerationByName("role", 20, MemberRole::class).default(MemberRole.player)
    val jerseyNumber = integer("jersey_number").nullable()
    val position = varchar("position", 50).nullable()

    val isActive = bool("is_active").default(true)
    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}

object TeamRegistrationsTable : Table("team_registrations") {
    val id = uuid("id")
    val tournamentId = uuid("tournament_id").references(TournamentsTable.id, onDelete = ReferenceOption.CASCADE)
    val teamId = uuid("team_id").references(TeamsTable.id, onDelete = ReferenceOption.CASCADE)
    val groupId = uuid("group_id").references(TournamentGroupsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    val registrationDate = timestamp("registration_date").defaultExpression(CurrentTimestamp())
    val status = enumerationByName("status", 20, RegistrationStatus::class).default(RegistrationStatus.pending)
    val paymentStatus = enumerationByName("payment_status", 20, PaymentStatus::class).default(PaymentStatus.unpaid)

    val additionalInfo = text("additional_info").nullable()
    val seedNumber = integer("seed_number").nullable()

    val approvedAt = timestamp("approved_at").nullable()
    val approvedBy = uuid("approved_by").references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    override val primaryKey = PrimaryKey(id)
}