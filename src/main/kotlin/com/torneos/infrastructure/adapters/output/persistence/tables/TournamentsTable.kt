package com.torneos.infrastructure.adapters.output.persistence.tables

import com.torneos.domain.enums.TournamentStatus
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object TournamentsTable : Table("tournaments") {
    val id = uuid("id")
    val organizerId = uuid("organizer_id").references(UsersTable.id, onDelete = ReferenceOption.RESTRICT)
    val sportId = uuid("sport_id").references(SportsTable.id, onDelete = ReferenceOption.RESTRICT).nullable()
    
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val sport = varchar("sport", 50)
    val sportSubType = varchar("sport_sub_type", 50).nullable()

    // Configuración
    val tournamentType = varchar("tournament_type", 50)
    val category = varchar("category", 50).nullable()
    val eliminationMode = varchar("elimination_mode", 50).nullable()

    // Fechas y Ubicación
    val location = varchar("location", 200).nullable()
    val startDate = timestamp("start_date")
    val endDate = timestamp("end_date").nullable()
    val registrationDeadline = timestamp("registration_deadline").nullable()

    // Capacidad y Costos
    val maxTeams = integer("max_teams")
    val currentTeams = integer("current_teams").default(0)
    val registrationFee = decimal("registration_fee", 10, 2).default(java.math.BigDecimal.ZERO)
    val prizePool = varchar("prize_pool", 100).nullable()

    // Privacidad
    val isPrivate = bool("is_private").default(false)
    val requiresApproval = bool("requires_approval").default(false)
    val accessCode = varchar("access_code", 50).nullable()

    // Grupos y Config (JSONB mapeado a Text)
    val hasGroupStage = bool("has_group_stage").default(false)
    val numberOfGroups = integer("number_of_groups").nullable()
    val teamsPerGroup = integer("teams_per_group").nullable()
    val teamsAdvancePerGroup = integer("teams_advance_per_group").nullable()
    
    val sportSettings = text("sport_settings").default("{}")
    val groupConfig = text("group_config").default("{}") // Si usas este campo en SQL

    // Puntuación
    val allowTies = bool("allow_ties").default(false)
    val pointsForWin = integer("points_for_win").default(3)
    val pointsForDraw = integer("points_for_draw").default(1)
    val pointsForLoss = integer("points_for_loss").default(0)

    val rulesText = text("rules").nullable() // Ojo: en SQL se llama 'rules', en modelo 'rulesText'
    val imageUrl = varchar("image_url", 255).nullable()

    val status = postgresEnumeration("status", "tournament_status", TournamentStatus::class.java)
        .default(TournamentStatus.upcoming)

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}