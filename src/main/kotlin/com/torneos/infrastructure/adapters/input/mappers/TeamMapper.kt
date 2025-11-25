package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Team
import com.torneos.domain.models.TeamMember
import com.torneos.infrastructure.adapters.input.dtos.CreateTeamRequest
import com.torneos.infrastructure.adapters.input.dtos.TeamResponse
import com.torneos.infrastructure.adapters.input.dtos.AddMemberRequest
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
        userId = null, // Se buscaría por email en el caso de uso si existe
        memberName = this.name,
        memberEmail = this.email,
        memberPhone = null,
        role = this.role,
        jerseyNumber = this.jerseyNumber,
        position = this.position,
                isActive = true

    )
}