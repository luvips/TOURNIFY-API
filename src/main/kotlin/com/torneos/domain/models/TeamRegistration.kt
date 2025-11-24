package com.torneos.domain.models

import com.torneos.domain.enums.PaymentStatus
import com.torneos.domain.enums.RegistrationStatus
import java.time.Instant
import java.util.UUID

data class TeamRegistration(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val teamId: UUID,
    val groupId: UUID?,
    
    val registrationDate: Instant = Instant.now(),
    val status: RegistrationStatus,
    val paymentStatus: PaymentStatus,
    
    val additionalInfo: String?,
    val seedNumber: Int?,
    
    val approvedAt: Instant?,
    val approvedBy: UUID?
)