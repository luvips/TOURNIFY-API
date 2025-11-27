package com.torneos.domain.models

import com.torneos.domain.enums.MemberRole
import java.time.Instant
import java.util.UUID

data class TeamMemberWithUser(
    val id: UUID,
    val teamId: UUID,
    val userId: UUID?,
    val name: String?,
    val email: String?,
    val role: MemberRole,
    val jerseyNumber: Int?,
    val position: String?,
    val joinedAt: Instant
)

data class TeamWithMembers(
    val team: Team,
    val members: List<TeamMemberWithUser>
)
