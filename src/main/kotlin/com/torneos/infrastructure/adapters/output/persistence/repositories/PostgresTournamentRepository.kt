package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.Tournament
import com.torneos.domain.models.TournamentFollower
import com.torneos.domain.ports.TournamentRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentFollowersTable
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresTournamentRepository : TournamentRepository {

    private fun ResultRow.toTournament() = Tournament(
        id = this[TournamentsTable.id],
        organizerId = this[TournamentsTable.organizerId],
        sportId = this[TournamentsTable.sportId],
        name = this[TournamentsTable.name],
        description = this[TournamentsTable.description],
        sport = this[TournamentsTable.sport],
        sportSubType = this[TournamentsTable.sportSubType],
        tournamentType = this[TournamentsTable.tournamentType],
        category = this[TournamentsTable.category],
        eliminationMode = this[TournamentsTable.eliminationMode],
        location = this[TournamentsTable.location],
        startDate = this[TournamentsTable.startDate],
        endDate = this[TournamentsTable.endDate],
        registrationDeadline = this[TournamentsTable.registrationDeadline],
        maxTeams = this[TournamentsTable.maxTeams],
        currentTeams = this[TournamentsTable.currentTeams],
        registrationFee = this[TournamentsTable.registrationFee],
        prizePool = this[TournamentsTable.prizePool],
        isPrivate = this[TournamentsTable.isPrivate],
        requiresApproval = this[TournamentsTable.requiresApproval],
        accessCode = this[TournamentsTable.accessCode],
        hasGroupStage = this[TournamentsTable.hasGroupStage],
        groupConfigJson = this[TournamentsTable.groupConfig],
        sportSettingsJson = this[TournamentsTable.sportSettings],
        allowTies = this[TournamentsTable.allowTies],
        pointsForWin = this[TournamentsTable.pointsForWin],
        pointsForDraw = this[TournamentsTable.pointsForDraw],
        pointsForLoss = this[TournamentsTable.pointsForLoss],
        rulesText = this[TournamentsTable.rulesText],
        imageUrl = this[TournamentsTable.imageUrl],
        status = this[TournamentsTable.status],
        createdAt = this[TournamentsTable.createdAt],
        updatedAt = this[TournamentsTable.updatedAt]
    )

    override suspend fun create(tournament: Tournament): Tournament = dbQuery {
        TournamentsTable.insert {
            it[id] = tournament.id
            it[organizerId] = tournament.organizerId
            it[sportId] = tournament.sportId
            it[name] = tournament.name
            it[sport] = tournament.sport
            it[sportSubType] = tournament.sportSubType
            it[tournamentType] = tournament.tournamentType
            it[category] = tournament.category
            it[eliminationMode] = tournament.eliminationMode
            it[location] = tournament.location
            it[startDate] = tournament.startDate
            it[endDate] = tournament.endDate
            it[registrationDeadline] = tournament.registrationDeadline
            it[maxTeams] = tournament.maxTeams
            it[registrationFee] = tournament.registrationFee
            it[prizePool] = tournament.prizePool
            it[isPrivate] = tournament.isPrivate
            it[requiresApproval] = tournament.requiresApproval
            it[accessCode] = tournament.accessCode
            it[hasGroupStage] = tournament.hasGroupStage
            it[groupConfig] = tournament.groupConfigJson
            it[sportSettings] = tournament.sportSettingsJson
            it[allowTies] = tournament.allowTies
            it[pointsForWin] = tournament.pointsForWin
            it[pointsForDraw] = tournament.pointsForDraw
            it[pointsForLoss] = tournament.pointsForLoss
            it[rulesText] = tournament.rulesText
            it[status] = tournament.status
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
            .orderBy(TournamentsTable.createdAt to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { it.toTournament() }
    }

    override suspend fun update(tournament: Tournament): Tournament? = dbQuery {
        val rows = TournamentsTable.update({ TournamentsTable.id eq tournament.id }) {
            it[name] = tournament.name
            it[status] = tournament.status
            it[currentTeams] = tournament.currentTeams
            // ... Mapear otros campos editables
        }
        if (rows > 0) tournament else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TournamentsTable.deleteWhere { TournamentsTable.id eq id } > 0
    }

    override suspend fun findByOrganizer(organizerId: UUID): List<Tournament> = dbQuery {
        TournamentsTable.selectAll().where { TournamentsTable.organizerId eq organizerId }
            .map { it.toTournament() }
    }

    // --- FOLLOWERS IMPLEMENTATION ---
    
    override suspend fun addFollower(follower: TournamentFollower): Boolean = dbQuery {
        try {
            TournamentFollowersTable.insert {
                it[userId] = follower.userId
                it[tournamentId] = follower.tournamentId
                it[followedAt] = follower.followedAt
            }
            true
        } catch (e: Exception) {
            false // Probablemente llave duplicada
        }
    }

    override suspend fun removeFollower(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.deleteWhere { 
            (TournamentFollowersTable.userId eq userId) and 
            (TournamentFollowersTable.tournamentId eq tournamentId) 
        } > 0
    }

    override suspend fun countFollowers(tournamentId: UUID): Long = dbQuery {
        TournamentFollowersTable.selectAll().where { TournamentFollowersTable.tournamentId eq tournamentId }
            .count()
    }

    override suspend fun isFollowing(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.selectAll()
            .where { (TournamentFollowersTable.userId eq userId) and (TournamentFollowersTable.tournamentId eq tournamentId) }
            .count() > 0
    }

    override suspend fun findFollowedByUser(userId: UUID): List<Tournament> = dbQuery {
        (TournamentsTable innerJoin TournamentFollowersTable)
            .selectAll().where { TournamentFollowersTable.userId eq userId }
            .map { it.toTournament() }
    }
}