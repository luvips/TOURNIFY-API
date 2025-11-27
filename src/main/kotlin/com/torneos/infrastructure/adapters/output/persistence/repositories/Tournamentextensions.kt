package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.enums.EliminationMode

fun EliminationMode?.toDbString(): String? {
    return this?.name
}


fun String?.toEliminationMode(): EliminationMode? {
    if (this == null) return null
    return try {
        EliminationMode.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}