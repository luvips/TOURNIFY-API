package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.Tournament
import com.torneos.domain.models.TournamentFollower
import com.torneos.domain.ports.TournamentRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentFollowersTable
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class PostgresTournamentRepository : TournamentRepository {

    private fun ResultRow.toTournament() = Tournament(
        id = this[TournamentsTable.id],
        name = this[TournamentsTable.name],
        description = this[TournamentsTable.description],
        sportId = this[TournamentsTable.sport_id],
        sport = this[TournamentsTable.sport],
        sportSubType = this[TournamentsTable.sport_sub_type],
        organizerId = this[TournamentsTable.organizer_id],
        tournamentType = this[TournamentsTable.tournament_type],
        category = this[TournamentsTable.category],
        eliminationMode = this[TournamentsTable.elimination_mode],
        location = this[TournamentsTable.location],
        startDate = this[TournamentsTable.start_date],
        endDate = this[TournamentsTable.end_date],
        registrationDeadline = this[TournamentsTable.registration_deadline],
        maxTeams = this[TournamentsTable.max_teams],
        currentTeams = this[TournamentsTable.current_teams],
        registrationFee = this[TournamentsTable.registration_fee],
        prizePool = this[TournamentsTable.prize_pool],
        isPrivate = this[TournamentsTable.is_private],
        requiresApproval = this[TournamentsTable.requires_approval],
        accessCode = this[TournamentsTable.access_code],
        hasGroupStage = this[TournamentsTable.has_group_stage],
        numberOfGroups = this[TournamentsTable.number_of_groups],
        teamsPerGroup = this[TournamentsTable.teams_per_group],
        teamsAdvancePerGroup = this[TournamentsTable.teams_advance_per_group],
        sportSettingsJson = this[TournamentsTable.sport_settings],
        allowTies = this[TournamentsTable.allow_ties],
        pointsForWin = this[TournamentsTable.points_for_win],
        pointsForDraw = this[TournamentsTable.points_for_draw],
        pointsForLoss = this[TournamentsTable.points_for_loss],
        rulesText = this[TournamentsTable.rules_text],
        imageUrl = this[TournamentsTable.image_url],
        status = this[TournamentsTable.status],
        createdAt = this[TournamentsTable.created_at],
        updatedAt = this[TournamentsTable.updated_at]
    )

    override suspend fun create(tournament: Tournament): Tournament = dbQuery {
        TournamentsTable.insert {
            it[id] = tournament.id
            it[name] = tournament.name
            it[description] = tournament.description
            it[sport_id] = tournament.sportId
            it[sport] = tournament.sport
            it[sport_sub_type] = tournament.sportSubType
            it[organizer_id] = tournament.organizerId
            it[tournament_type] = tournament.tournamentType
            it[category] = tournament.category
            it[elimination_mode] = tournament.eliminationMode
            it[location] = tournament.location
            it[start_date] = tournament.startDate
            it[end_date] = tournament.endDate
            it[registration_deadline] = tournament.registrationDeadline
            it[max_teams] = tournament.maxTeams
            it[current_teams] = tournament.currentTeams
            it[registration_fee] = tournament.registrationFee
            it[prize_pool] = tournament.prizePool
            it[is_private] = tournament.isPrivate
            it[requires_approval] = tournament.requiresApproval
            it[access_code] = tournament.accessCode
            it[has_group_stage] = tournament.hasGroupStage
            it[number_of_groups] = tournament.numberOfGroups
            it[teams_per_group] = tournament.teamsPerGroup
            it[teams_advance_per_group] = tournament.teamsAdvancePerGroup
            it[sport_settings] = tournament.sportSettingsJson
            it[allow_ties] = tournament.allowTies
            it[points_for_win] = tournament.pointsForWin
            it[points_for_draw] = tournament.pointsForDraw
            it[points_for_loss] = tournament.pointsForLoss
            it[rules_text] = tournament.rulesText
            it[image_url] = tournament.imageUrl
            it[status] = tournament.status
            it[created_at] = tournament.createdAt
            it[updated_at] = tournament.updatedAt
        }
        tournament
    }

    override suspend fun findById(id: UUID): Tournament? = dbQuery {
        TournamentsTable.selectAll().where { TournamentsTable.id eq id }
            .map { it.toTournament() }
            .singleOrNull()
    }

    override suspend fun findAll(page: Int, pageSize: Int): List<Tournament> = dbQuery {
        TournamentsTable.selectAll()
            .orderBy(TournamentsTable.created_at to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { it.toTournament() }
    }

    override suspend fun update(tournament: Tournament): Tournament? = dbQuery {
        val updatedRows = TournamentsTable.update({ TournamentsTable.id eq tournament.id }) {
            it[name] = tournament.name
            it[description] = tournament.description
            it[status] = tournament.status
            it[start_date] = tournament.startDate
            it[end_date] = tournament.endDate
            it[registration_deadline] = tournament.registrationDeadline
            it[max_teams] = tournament.maxTeams
            it[location] = tournament.location
            it[elimination_mode] = tournament.eliminationMode
            it[category] = tournament.category
            it[is_private] = tournament.isPrivate
            it[requires_approval] = tournament.requiresApproval
            it[access_code] = tournament.accessCode
            it[has_group_stage] = tournament.hasGroupStage
            it[number_of_groups] = tournament.numberOfGroups
            it[teams_per_group] = tournament.teamsPerGroup
            it[teams_advance_per_group] = tournament.teamsAdvancePerGroup
            it[allow_ties] = tournament.allowTies
            it[points_for_win] = tournament.pointsForWin
            it[points_for_draw] = tournament.pointsForDraw
            it[points_for_loss] = tournament.pointsForLoss
            it[image_url] = tournament.imageUrl
            it[rules_text] = tournament.rulesText
            it[sport_settings] = tournament.sportSettingsJson
            it[updated_at] = Instant.now()
        }
        if (updatedRows > 0) findById(tournament.id) else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TournamentsTable.deleteWhere { TournamentsTable.id eq id } > 0
    }

    override suspend fun findByOrganizer(organizerId: UUID): List<Tournament> = dbQuery {
        TournamentsTable.selectAll().where { TournamentsTable.organizer_id eq organizerId }
            .map { it.toTournament() }
    }

    override suspend fun addFollower(follower: TournamentFollower): Boolean = dbQuery {
        try {
            TournamentFollowersTable.insert {
                it[userId] = follower.userId
                it[tournamentId] = follower.tournamentId
                it[followedAt] = follower.followedAt
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeFollower(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.deleteWhere {
            (TournamentFollowersTable.userId eq userId) and (TournamentFollowersTable.tournamentId eq tournamentId)
        } > 0
    }

    override suspend fun countFollowers(tournamentId: UUID): Long = dbQuery {
        TournamentFollowersTable.selectAll().where { TournamentFollowersTable.tournamentId eq tournamentId }
            .count()
    }

    override suspend fun isFollowing(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.selectAll().where {
            (TournamentFollowersTable.userId eq userId) and (TournamentFollowersTable.tournamentId eq tournamentId)
        }.count() > 0
    }

    override suspend fun findFollowedByUser(userId: UUID): List<Tournament> = dbQuery {
        (TournamentsTable innerJoin TournamentFollowersTable)
            .selectAll().where { TournamentFollowersTable.userId eq userId }
            .map { it.toTournament() }
    }
}