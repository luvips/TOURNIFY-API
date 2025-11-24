package com.torneos.domain.models

import com.torneos.domain.enums.RegistrationStatus
import java.time.Instant
import java.util.UUID

data class Registration(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val teamId: UUID,
    val status: RegistrationStatus,
    val paymentStatus: Boolean,
    val registeredAt: Instant = Instant.now()
)