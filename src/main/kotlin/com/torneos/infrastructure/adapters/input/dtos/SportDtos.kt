package com.torneos.infrastructure.adapters.input.dtos

import com.torneos.domain.enums.SportCategory
import kotlinx.serialization.Serializable

@Serializable
data class SportResponse(
    val id: String,
    val name: String,
    val category: SportCategory,
    val iconUrl: String?
)

@Serializable
data class CreateSportRequest(
    val name: String,
    val category: SportCategory,
    val defaultPlayers: Int?,
    val defaultDuration: Int?
)