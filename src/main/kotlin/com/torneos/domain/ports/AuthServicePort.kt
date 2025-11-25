package com.torneos.domain.ports

interface AuthServicePort {
    fun hashPassword(password: String): String
    fun verifyPassword(password: String, hash: String): Boolean
    fun generateToken(userId: String, role: String): String
}