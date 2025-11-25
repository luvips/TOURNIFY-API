package com.torneos.infrastructure.adapters.output.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.torneos.domain.ports.AuthServicePort
import io.ktor.server.config.*
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

class BCryptAuthService(config: ApplicationConfig) : AuthServicePort {

    private val jwtSecret = config.property("jwt.secret").getString()
    private val jwtDomain = config.property("jwt.domain").getString()
    private val jwtAudience = config.property("jwt.audience").getString()
    // Expiración: 24 horas (en milisegundos)
    private val expirationTime = 24 * 60 * 60 * 1000

    override fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    override fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }

    override fun generateToken(userId: String, role: String): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .withClaim("id", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationTime))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}