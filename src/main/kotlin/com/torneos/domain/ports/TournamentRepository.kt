package com.torneos.domain.ports

import com.torneos.domain.models.Tournament
import com.torneos.domain.models.TournamentFollower
import java.util.UUID

interface TournamentRepository {
    // CRUD Básico
    suspend fun create(tournament: Tournament): Tournament
    suspend fun findById(id: UUID): Tournament?
    suspend fun findAll(page: Int = 1, pageSize: Int = 20): List<Tournament>
    suspend fun update(tournament: Tournament): Tournament?
    suspend fun delete(id: UUID): Boolean

    // Consultas Específicas
    suspend fun findByOrganizer(organizerId: UUID): List<Tournament>

    // Followers (Seguir Torneos)
    suspend fun addFollower(follower: TournamentFollower): Boolean
    suspend fun removeFollower(userId: UUID, tournamentId: UUID): Boolean
    suspend fun countFollowers(tournamentId: UUID): Long
    suspend fun isFollowing(userId: UUID, tournamentId: UUID): Boolean
    suspend fun findFollowedByUser(userId: UUID): List<Tournament>
}