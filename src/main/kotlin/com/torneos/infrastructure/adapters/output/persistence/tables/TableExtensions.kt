package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

// Helper genérico para mapear ENUMs de Postgres
fun <T : Enum<T>> Table.postgresEnumeration(
    name: String,
    postgresTypeName: String,
    enumClass: Class<T>
) = customEnumeration(
    name,
    postgresTypeName,
    { value -> java.lang.Enum.valueOf(enumClass, value as String) },
    { PGobject().apply { type = postgresTypeName; value = it.name } }
)