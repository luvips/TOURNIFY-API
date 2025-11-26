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