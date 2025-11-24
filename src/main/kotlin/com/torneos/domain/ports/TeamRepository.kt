package com.torneos.domain.ports

import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import java.util.UUID

interface TeamRepository {
    // Teams
    suspend fun create(team: Team): Team
    suspend fun findById(id: UUID): Team?
    suspend fun findAll(): List<Team>
    suspend fun update(team: Team): Team?
    suspend fun delete(id: UUID): Boolean

    // Members
    suspend fun addMember(member: TeamMember): TeamMember
    suspend fun getMembers(teamId: UUID): List<TeamMember>
    suspend fun removeMember(memberId: UUID): Boolean
}