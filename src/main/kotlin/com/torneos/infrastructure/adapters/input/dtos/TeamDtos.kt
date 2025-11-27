package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.MemberRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateTeamRequest(
    val name: String,
    val shortName: String?,
    val description: String?,
    val contactEmail: String?,
    val contactPhone: String?
)

@Serializable
data class TeamResponse(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val captainId: String?
)

@Serializable
data class AddMemberRequest(
    val email: String,
    val name: String? = null,
    val role: MemberRole = MemberRole.player,
    val jerseyNumber: Int? = null,
    val position: String? = null
)

@Serializable
data class TeamMemberWithUserResponse(
    val id: String,
    val userId: String?,
    val teamId: String,
    val name: String?,
    val email: String?,
    val role: String,
    val jerseyNumber: Int?,
    val position: String?,
    val joinedAt: String
)

@Serializable
data class TeamWithMembersResponse(
    val id: String,
    val name: String,
    val shortName: String?,
    val description: String?,
    val captainId: String?,
    val logoUrl: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val members: List<TeamMemberWithUserResponse>
)