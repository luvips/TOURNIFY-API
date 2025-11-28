package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.User
import com.torneos.domain.ports.UserRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class PostgresUserRepository : UserRepository {

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        username = this[UsersTable.username],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        role = UserRole.valueOf(this[UsersTable.role]),
        firstName = this[UsersTable.firstName],
        lastName = this[UsersTable.lastName],
        phone = this[UsersTable.phone],
        avatarUrl = this[UsersTable.avatarUrl],
        isActive = this[UsersTable.isActive],
        emailVerified = this[UsersTable.emailVerified],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt]
    )

    override suspend fun create(user: User): User = dbQuery {
        UsersTable.insert {
            it[id] = user.id
            it[username] = user.username
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[role] = user.role.name
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[phone] = user.phone
            it[avatarUrl] = user.avatarUrl
            it[isActive] = user.isActive
        }
        user
    }

    override suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByRole(role: UserRole): List<User> = dbQuery {
        UsersTable.selectAll().where { UsersTable.role eq role.name }
            .map { it.toUser() }
    }

    override suspend fun update(user: User): User? = dbQuery {
        val updatedRows = UsersTable.update({ UsersTable.id eq user.id }) {
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[phone] = user.phone
            it[avatarUrl] = user.avatarUrl
            it[isActive] = user.isActive
            it[passwordHash] = user.passwordHash
            it[role] = user.role.name
        }
        if (updatedRows > 0) user else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }
}