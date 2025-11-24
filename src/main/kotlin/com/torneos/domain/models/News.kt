package com.torneos.domain.models

import java.time.Instant
import java.util.UUID

data class News(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val content: String,
    val imageUrl: String?,
    val category: String?,
    val isFeatured: Boolean,
    val createdAt: Instant = Instant.now()
)