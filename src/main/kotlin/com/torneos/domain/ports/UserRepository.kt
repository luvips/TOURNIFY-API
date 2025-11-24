package com.torneos.domain.ports

import com.torneos.domain.models.User
import java.util.UUID

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findAll(): List<User>
    suspend fun update(user: User): User?
    suspend fun delete(id: UUID): Boolean
}