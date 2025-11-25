package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.enums.RegistrationStatus
import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.ports.RegistrationRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TeamRegistrationsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresRegistrationRepository : RegistrationRepository {

    private fun ResultRow.toTeamRegistration() = TeamRegistration(
        id = this[TeamRegistrationsTable.id],
        tournamentId = this[TeamRegistrationsTable.tournamentId],
        teamId = this[TeamRegistrationsTable.teamId],
        groupId = this[TeamRegistrationsTable.groupId],
        registrationDate = this[TeamRegistrationsTable.registrationDate],
        status = this[TeamRegistrationsTable.status],
        paymentStatus = this[TeamRegistrationsTable.paymentStatus],
        additionalInfo = this[TeamRegistrationsTable.additionalInfo],
        seedNumber = this[TeamRegistrationsTable.seedNumber],
        approvedAt = this[TeamRegistrationsTable.approvedAt],
        approvedBy = this[TeamRegistrationsTable.approvedBy]
    )

    override suspend fun create(registration: TeamRegistration): TeamRegistration = dbQuery {
        TeamRegistrationsTable.insert {
            it[id] = registration.id
            it[tournamentId] = registration.tournamentId
            it[teamId] = registration.teamId
            it[groupId] = registration.groupId
            it[status] = registration.status
            it[paymentStatus] = registration.paymentStatus
            it[additionalInfo] = registration.additionalInfo
            it[seedNumber] = registration.seedNumber
            it[approvedAt] = registration.approvedAt
            it[approvedBy] = registration.approvedBy
        }
        registration
    }

    override suspend fun findById(id: UUID): TeamRegistration? = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.id eq id }
            .map { it.toTeamRegistration() }
            .singleOrNull()
    }

    override suspend fun findAll(): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll()
            .map { it.toTeamRegistration() }
    }

    override suspend fun update(registration: TeamRegistration): TeamRegistration? = dbQuery {
        val rows = TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq registration.id }) {
            it[status] = registration.status
            it[paymentStatus] = registration.paymentStatus
            it[groupId] = registration.groupId
            it[seedNumber] = registration.seedNumber
            it[approvedAt] = registration.approvedAt
            it[approvedBy] = registration.approvedBy
        }
        if (rows > 0) registration else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TeamRegistrationsTable.deleteWhere { TeamRegistrationsTable.id eq id } > 0
    }

    override suspend fun findByTournamentId(tournamentId: UUID): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.tournamentId eq tournamentId }
            .map { it.toTeamRegistration() }
    }

    override suspend fun findByTeamId(teamId: UUID): List<TeamRegistration> = dbQuery {
        TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.teamId eq teamId }
            .map { it.toTeamRegistration() }
    }

    override suspend fun findByTournamentAndTeam(tournamentId: UUID, teamId: UUID): TeamRegistration? = dbQuery {
        TeamRegistrationsTable.selectAll()
            .where {
                (TeamRegistrationsTable.tournamentId eq tournamentId) and
                (TeamRegistrationsTable.teamId eq teamId)
            }
            .map { it.toTeamRegistration() }
            .singleOrNull()
    }

    override suspend fun updateStatus(id: UUID, status: RegistrationStatus): Boolean = dbQuery {
        TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq id }) {
            it[TeamRegistrationsTable.status] = status
        } > 0
    }

    override suspend fun updatePaymentStatus(id: UUID, status: PaymentStatus): Boolean = dbQuery {
        TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq id }) {
            it[paymentStatus] = status
        } > 0
    }
}
