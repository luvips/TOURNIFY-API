package com.torneos.infrastructure.adapters.input.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AssignRefereeRequest(
    val refereeId: String,
    val tournamentId: String,
    val notes: String? = null
)
