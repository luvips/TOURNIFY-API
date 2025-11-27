package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import com.torneos.domain.models.TeamMemberWithUser
import com.torneos.domain.models.TeamRegistration
import com.torneos.domain.models.TeamWithMembers
import com.torneos.domain.ports.TeamRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TeamMembersTable
import com.torneos.infrastructure.adapters.output.persistence.tables.TeamRegistrationsTable
import com.torneos.infrastructure.adapters.output.persistence.tables.TeamsTable
import com.torneos.infrastructure.adapters.output.persistence.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresTeamRepository : TeamRepository {

    private fun ResultRow.toTeam() = Team(
        id = this[TeamsTable.id],
        name = this[TeamsTable.name],
        shortName = this[TeamsTable.shortName],
        logoUrl = this[TeamsTable.logoUrl],
        captainId = this[TeamsTable.captainId],
        description = this[TeamsTable.description],
        contactEmail = this[TeamsTable.contactEmail],
        contactPhone = this[TeamsTable.contactPhone],
        isActive = this[TeamsTable.isActive],
        createdAt = this[TeamsTable.createdAt],
        updatedAt = this[TeamsTable.updatedAt]
    )

    override suspend fun create(team: Team): Team = dbQuery {
        TeamsTable.insert {
            it[id] = team.id
            it[name] = team.name
            it[shortName] = team.shortName
            it[captainId] = team.captainId
            it[description] = team.description
            it[contactEmail] = team.contactEmail
            it[contactPhone] = team.contactPhone
        }
        team
    }

    override suspend fun findById(id: UUID): Team? = dbQuery {
        TeamsTable.selectAll().where { TeamsTable.id eq id }
            .map { it.toTeam() }
            .singleOrNull()
    }
    
    override suspend fun findByCaptain(captainId: UUID): List<Team> = dbQuery {
        TeamsTable.selectAll().where { TeamsTable.captainId eq captainId }
            .map { it.toTeam() }
    }

    override suspend fun update(team: Team): Team? = dbQuery {
        val rows = TeamsTable.update({ TeamsTable.id eq team.id }) {
            it[name] = team.name
            it[logoUrl] = team.logoUrl
            it[contactEmail] = team.contactEmail
        }
        if (rows > 0) team else null
    }
    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TeamsTable.deleteWhere { TeamsTable.id eq id } > 0
    }

    override suspend fun getTeamWithMembers(teamId: UUID): TeamWithMembers? = dbQuery {
        // Obtener el equipo
        val team = TeamsTable.selectAll().where { TeamsTable.id eq teamId }
            .map { it.toTeam() }
            .singleOrNull() ?: return@dbQuery null

        // Obtener miembros con JOIN a Users para obtener nombre y email
        val members = (TeamMembersTable leftJoin UsersTable)
            .selectAll()
            .where { TeamMembersTable.teamId eq teamId }
            .map { row ->
                TeamMemberWithUser(
                    id = row[TeamMembersTable.id],
                    teamId = row[TeamMembersTable.teamId],
                    userId = row[TeamMembersTable.userId],
                    name = row.getOrNull(UsersTable.firstName)?.let { firstName ->
                        val lastName = row.getOrNull(UsersTable.lastName) ?: ""
                        "$firstName $lastName".trim()
                    } ?: row[TeamMembersTable.memberName],
                    email = row.getOrNull(UsersTable.email) ?: row[TeamMembersTable.memberEmail],
                    role = row[TeamMembersTable.role],
                    jerseyNumber = row[TeamMembersTable.jerseyNumber],
                    position = row[TeamMembersTable.position],
                    joinedAt = row[TeamMembersTable.joinedAt]
                )
            }

        TeamWithMembers(team, members)
    }

    override suspend fun isMemberOfTeam(teamId: UUID, userId: UUID): Boolean = dbQuery {
        TeamMembersTable.selectAll()
            .where { 
                (TeamMembersTable.teamId eq teamId) and 
                (TeamMembersTable.userId eq userId) 
            }
            .count() > 0
    }
    
    // --- MEMBERS ---
    override suspend fun addMember(member: TeamMember): TeamMember = dbQuery {
        TeamMembersTable.insert {
            it[id] = member.id
            it[teamId] = member.teamId
            it[userId] = member.userId
            it[memberName] = member.memberName
            it[memberEmail] = member.memberEmail
            it[role] = member.role
            it[jerseyNumber] = member.jerseyNumber
            it[position] = member.position
        }
        member
    }

    override suspend fun removeMember(memberId: UUID): Boolean = dbQuery {
        TeamMembersTable.deleteWhere { TeamMembersTable.id eq memberId } > 0
    }

    override suspend fun getMembers(teamId: UUID): List<TeamMember> = dbQuery {
        TeamMembersTable.selectAll().where { TeamMembersTable.teamId eq teamId }
            .map { 
                TeamMember(
                    id = it[TeamMembersTable.id],
                    teamId = it[TeamMembersTable.teamId],
                    userId = it[TeamMembersTable.userId],
                    memberName = it[TeamMembersTable.memberName],
                    memberEmail = it[TeamMembersTable.memberEmail],
                    memberPhone = it[TeamMembersTable.memberPhone],
                    role = it[TeamMembersTable.role],
                    jerseyNumber = it[TeamMembersTable.jerseyNumber],
                    position = it[TeamMembersTable.position],
                    isActive = it[TeamMembersTable.isActive],
                    joinedAt = it[TeamMembersTable.joinedAt]
                )
            }
    }

    override suspend fun registerToTournament(registration: TeamRegistration): TeamRegistration = dbQuery {
        TeamRegistrationsTable.insert {
            it[id] = registration.id
            it[tournamentId] = registration.tournamentId
            it[teamId] = registration.teamId
            it[status] = registration.status
            it[paymentStatus] = registration.paymentStatus
            it[registrationDate] = registration.registrationDate
        }
        registration
    }
    
    override suspend fun getTournamentRegistrations(tournamentId: UUID): List<TeamRegistration> = dbQuery {
         TeamRegistrationsTable.selectAll().where { TeamRegistrationsTable.tournamentId eq tournamentId }
            .map {
                TeamRegistration(
                    id = it[TeamRegistrationsTable.id],
                    tournamentId = it[TeamRegistrationsTable.tournamentId],
                    teamId = it[TeamRegistrationsTable.teamId],
                    groupId = it[TeamRegistrationsTable.groupId],
                    registrationDate = it[TeamRegistrationsTable.registrationDate],
                    status = it[TeamRegistrationsTable.status],
                    paymentStatus = it[TeamRegistrationsTable.paymentStatus],
                    additionalInfo = it[TeamRegistrationsTable.additionalInfo],
                    seedNumber = it[TeamRegistrationsTable.seedNumber],
                    approvedAt = it[TeamRegistrationsTable.approvedAt],
                    approvedBy = it[TeamRegistrationsTable.approvedBy]
                )
            }
    }

    override suspend fun updateRegistrationStatus(registrationId: UUID, status: com.torneos.domain.enums.RegistrationStatus): Boolean = dbQuery {
        TeamRegistrationsTable.update({ TeamRegistrationsTable.id eq registrationId }) {
            it[TeamRegistrationsTable.status] = status
        } > 0
    }
}