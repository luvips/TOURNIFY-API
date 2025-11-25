package com.torneos.infrastructure.adapters.output.persistence.tables

import com.torneos.domain.enums.UserRole
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    
    val role = postgresEnumeration("role", "user_role", UserRole::class.java)
        .default(UserRole.player)

    val firstName = varchar("first_name", 50).nullable()
    val lastName = varchar("last_name", 50).nullable()
    val phone = varchar("phone", 20).nullable()
    val avatarUrl = text("avatar_url").nullable() // Text para URLs largas
    
    val isActive = bool("is_active").default(true)
    val emailVerified = bool("email_verified").default(false)
    
    val createdAt = timestamp("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}