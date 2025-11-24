package com.torneos.domain.models

import com.torneos.domain.enums.MemberRole
import java.time.Instant
import java.util.UUID

data class TeamMember(
    val id: UUID = UUID.randomUUID(),
    val teamId: UUID,
    val userId: UUID?, // Null si es un usuario "invitado" no registrado en la app
    
    val memberName: String?,
    val memberEmail: String?,
    val memberPhone: String?,
    
    val role: MemberRole,
    val jerseyNumber: Int?,
    val position: String?,
    
    val isActive: Boolean,
    val joinedAt: Instant = Instant.now()
)