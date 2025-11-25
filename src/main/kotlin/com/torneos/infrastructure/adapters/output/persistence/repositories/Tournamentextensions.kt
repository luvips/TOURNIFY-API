package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.enums.EliminationMode

/**
 * Extensiones para convertir EliminationMode entre String (BD) y Enum (Dominio)
 * Necesario porque PostgreSQL no puede mapear enums de Kotlin directamente
 * cuando se usa varchar en lugar de tipo ENUM nativo.
 */

/**
 * Convierte un EliminationMode a String para guardar en BD
 */
fun EliminationMode?.toDbString(): String? {
    return this?.name
}

/**
 * Convierte un String de BD a EliminationMode enum
 */
fun String?.toEliminationMode(): EliminationMode? {
    if (this == null) return null
    return try {
        EliminationMode.valueOf(this)
    } catch (e: IllegalArgumentException) {
        null // Si el string no es válido, retorna null
    }
}