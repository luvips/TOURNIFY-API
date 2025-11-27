package com.torneos.infrastructure.adapters.output.persistence.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    
    val role = varchar("role", 20).default("player")

    val firstName = varchar("first_name", 50).nullable()
    val lastName = varchar("last_name", 50).nullable()
    val phone = varchar("phone", 20).nullable()
    val avatarUrl = text("avatar_url").nullable()
    
    val isActive = bool("is_active").default(true)
    val emailVerified = bool("email_verified").default(false)
    
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}