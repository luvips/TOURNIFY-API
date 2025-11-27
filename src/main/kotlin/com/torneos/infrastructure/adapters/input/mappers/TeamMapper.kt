package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import com.torneos.domain.models.TeamMemberWithUser
import com.torneos.domain.models.TeamWithMembers
import com.torneos.infrastructure.adapters.input.dtos.CreateTeamRequest
import com.torneos.infrastructure.adapters.input.dtos.TeamResponse
import com.torneos.infrastructure.adapters.input.dtos.AddMemberRequest
import com.torneos.infrastructure.adapters.input.dtos.TeamMemberWithUserResponse
import com.torneos.infrastructure.adapters.input.dtos.TeamWithMembersResponse
import java.util.UUID

fun CreateTeamRequest.toDomain(captainId: UUID): Team {
    return Team(
        name = this.name,
        shortName = this.shortName,
        description = this.description,
        contactEmail = this.contactEmail,
        contactPhone = this.contactPhone,
        captainId = captainId,
        logoUrl = null, 
        isActive = true
    )
}

fun Team.toResponse(): TeamResponse {
    return TeamResponse(
        id = this.id.toString(),
        name = this.name,
        logoUrl = this.logoUrl,
        captainId = this.captainId?.toString()
    )
}

fun AddMemberRequest.toDomain(teamId: UUID): TeamMember {
    return TeamMember(
        teamId = teamId,
        userId = null,
        memberName = this.name, // Pasamos lo que venga (null o texto)
        memberEmail = this.email,
        memberPhone = null,
        role = this.role,
        jerseyNumber = this.jerseyNumber,
        position = this.position,
        isActive = true
    )
}

fun TeamMemberWithUser.toResponse(): TeamMemberWithUserResponse {
    return TeamMemberWithUserResponse(
        id = this.id.toString(),
        userId = this.userId?.toString(),
        teamId = this.teamId.toString(),
        name = this.name,
        email = this.email,
        role = this.role.name,
        jerseyNumber = this.jerseyNumber,
        position = this.position,
        joinedAt = this.joinedAt.toString()
    )
}

fun TeamWithMembers.toResponse(): TeamWithMembersResponse {
    return TeamWithMembersResponse(
        id = this.team.id.toString(),
        name = this.team.name,
        shortName = this.team.shortName,
        description = this.team.description,
        captainId = this.team.captainId?.toString(),
        logoUrl = this.team.logoUrl,
        contactEmail = this.team.contactEmail,
        contactPhone = this.team.contactPhone,
        members = this.members.map { it.toResponse() }
    )
}

