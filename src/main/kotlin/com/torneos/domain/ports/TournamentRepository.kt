package com.torneos.domain.ports

import com.torneos.domain.models.Tournament
import java.util.UUID

interface TournamentRepository {
    suspend fun create(tournament: Tournament): Tournament
    suspend fun findById(id: UUID): Tournament?
    suspend fun findAll(): List<Tournament>
    suspend fun update(tournament: Tournament): Tournament?
    suspend fun delete(id: UUID): Boolean
    suspend fun findByOrganizer(organizerId: UUID): List<Tournament>
}