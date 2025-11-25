package com.torneos.infrastructure.adapters.output.persistence.repositories

import com.torneos.domain.models.Tournament
import com.torneos.domain.models.TournamentFollower
import com.torneos.domain.ports.TournamentRepository
import com.torneos.infrastructure.adapters.output.persistence.DatabaseFactory.dbQuery
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentFollowersTable
import com.torneos.infrastructure.adapters.output.persistence.tables.TournamentsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class PostgresTournamentRepository : TournamentRepository {

    /**
     * Mapea una fila de BD al modelo de dominio Tournament
     * ✅ CORREGIDO: Incluye todos los campos, usa campos separados en vez de groupConfigJson
     */
    private fun ResultRow.toTournament() = Tournament(
        id = this[TournamentsTable.id],
        organizerId = this[TournamentsTable.organizerId],
        sportId = this[TournamentsTable.sportId],
        name = this[TournamentsTable.name],
        description = this[TournamentsTable.description],
        sport = this[TournamentsTable.sport],
        sportSubType = this[TournamentsTable.sportSubType],
        tournamentType = this[TournamentsTable.tournamentType],
        category = this[TournamentsTable.category],
        
        // ✅ CORREGIDO: Conversión de String a EliminationMode usando helper
        eliminationMode = this[TournamentsTable.eliminationMode].toEliminationMode(),
        
        location = this[TournamentsTable.location],
        startDate = this[TournamentsTable.startDate],
        endDate = this[TournamentsTable.endDate],
        registrationDeadline = this[TournamentsTable.registrationDeadline],
        maxTeams = this[TournamentsTable.maxTeams],
        currentTeams = this[TournamentsTable.currentTeams],
        registrationFee = this[TournamentsTable.registrationFee],
        prizePool = this[TournamentsTable.prizePool],
        isPrivate = this[TournamentsTable.isPrivate],
        requiresApproval = this[TournamentsTable.requiresApproval],
        accessCode = this[TournamentsTable.accessCode],
        hasGroupStage = this[TournamentsTable.hasGroupStage],
        
        // ✅ CORREGIDO: Mapear campos separados en vez de JSON
        numberOfGroups = this[TournamentsTable.numberOfGroups],
        teamsPerGroup = this[TournamentsTable.teamsPerGroup],
        teamsAdvancePerGroup = this[TournamentsTable.teamsAdvancePerGroup],
        
        sportSettingsJson = this[TournamentsTable.sportSettings],
        allowTies = this[TournamentsTable.allowTies],
        pointsForWin = this[TournamentsTable.pointsForWin],
        pointsForDraw = this[TournamentsTable.pointsForDraw],
        pointsForLoss = this[TournamentsTable.pointsForLoss],
        rulesText = this[TournamentsTable.rulesText],
        imageUrl = this[TournamentsTable.imageUrl],
        status = this[TournamentsTable.status],
        createdAt = this[TournamentsTable.createdAt],
        updatedAt = this[TournamentsTable.updatedAt]
    )

    /**
     * ✅ CORREGIDO: Crear torneo con TODOS los campos
     * Antes faltaban: description, imageUrl, numberOfGroups, teamsPerGroup, teamsAdvancePerGroup
     */
    override suspend fun create(tournament: Tournament): Tournament = dbQuery {
        TournamentsTable.insert {
            it[id] = tournament.id
            it[organizerId] = tournament.organizerId
            it[sportId] = tournament.sportId ?: throw IllegalArgumentException("Sport ID is required")
            it[name] = tournament.name
            // ✅ AGREGADO: description (antes faltaba)
            it[description] = tournament.description
            
            it[sport] = tournament.sport
            it[sportSubType] = tournament.sportSubType
            it[tournamentType] = tournament.tournamentType
            it[category] = tournament.category
            
            // ✅ CORREGIDO: Conversión de Enum a String
            it[eliminationMode] = tournament.eliminationMode.toDbString()
            
            it[location] = tournament.location
            it[startDate] = tournament.startDate
            it[endDate] = tournament.endDate
            it[registrationDeadline] = tournament.registrationDeadline
            it[maxTeams] = tournament.maxTeams
            it[currentTeams] = tournament.currentTeams
            it[registrationFee] = tournament.registrationFee
            it[prizePool] = tournament.prizePool
            it[isPrivate] = tournament.isPrivate
            it[requiresApproval] = tournament.requiresApproval
            it[accessCode] = tournament.accessCode
            it[hasGroupStage] = tournament.hasGroupStage
            
            // ✅ AGREGADO: Campos de configuración de grupos (antes faltaban)
            it[numberOfGroups] = tournament.numberOfGroups
            it[teamsPerGroup] = tournament.teamsPerGroup
            it[teamsAdvancePerGroup] = tournament.teamsAdvancePerGroup
            
            it[sportSettings] = tournament.sportSettingsJson
            it[allowTies] = tournament.allowTies
            it[pointsForWin] = tournament.pointsForWin
            it[pointsForDraw] = tournament.pointsForDraw
            it[pointsForLoss] = tournament.pointsForLoss
            it[rulesText] = tournament.rulesText
            
            // ✅ AGREGADO: imageUrl (antes faltaba)
            it[imageUrl] = tournament.imageUrl
            
            it[status] = tournament.status
        }
        tournament
    }

    override suspend fun findById(id: UUID): Tournament? = dbQuery {
        TournamentsTable.selectAll().where { TournamentsTable.id eq id }
            .map { it.toTournament() }
            .singleOrNull()
    }

    override suspend fun findAll(page: Int, pageSize: Int): List<Tournament> = dbQuery {
        TournamentsTable.selectAll()
            .orderBy(TournamentsTable.createdAt to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { it.toTournament() }
    }

    /**
     * ✅ CORREGIDO: UPDATE con TODOS los campos editables
     * Antes solo actualizaba: name, status, currentTeams
     */
    override suspend fun update(tournament: Tournament): Tournament? = dbQuery {
        val rows = TournamentsTable.update({ TournamentsTable.id eq tournament.id }) {
            // Básicos
            it[name] = tournament.name
            it[description] = tournament.description
            it[status] = tournament.status
            
            // Fechas
            it[startDate] = tournament.startDate
            it[endDate] = tournament.endDate
            it[registrationDeadline] = tournament.registrationDeadline
            
            // Capacidad y Costos
            it[maxTeams] = tournament.maxTeams
            it[currentTeams] = tournament.currentTeams
            it[registrationFee] = tournament.registrationFee
            it[prizePool] = tournament.prizePool
            
            // Configuración
            it[location] = tournament.location
            it[eliminationMode] = tournament.eliminationMode.toDbString()
            it[category] = tournament.category
            
            // Privacidad
            it[isPrivate] = tournament.isPrivate
            it[requiresApproval] = tournament.requiresApproval
            it[accessCode] = tournament.accessCode
            
            // Grupos
            it[hasGroupStage] = tournament.hasGroupStage
            it[numberOfGroups] = tournament.numberOfGroups
            it[teamsPerGroup] = tournament.teamsPerGroup
            it[teamsAdvancePerGroup] = tournament.teamsAdvancePerGroup
            
            // Reglas
            it[allowTies] = tournament.allowTies
            it[pointsForWin] = tournament.pointsForWin
            it[pointsForDraw] = tournament.pointsForDraw
            it[pointsForLoss] = tournament.pointsForLoss
            
            // Media
            it[imageUrl] = tournament.imageUrl
            it[rulesText] = tournament.rulesText
            it[sportSettings] = tournament.sportSettingsJson
            
            // ✅ CRÍTICO: Actualizar timestamp de modificación
            it[updatedAt] = Instant.now()
        }
        if (rows > 0) tournament else null
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        TournamentsTable.deleteWhere { TournamentsTable.id eq id } > 0
    }

    override suspend fun findByOrganizer(organizerId: UUID): List<Tournament> = dbQuery {
        TournamentsTable.selectAll().where { TournamentsTable.organizerId eq organizerId }
            .map { it.toTournament() }
    }

    // --- FOLLOWERS IMPLEMENTATION ---
    
    override suspend fun addFollower(follower: TournamentFollower): Boolean = dbQuery {
        try {
            TournamentFollowersTable.insert {
                it[userId] = follower.userId
                it[tournamentId] = follower.tournamentId
                it[followedAt] = follower.followedAt
            }
            true
        } catch (e: Exception) {
            false // Probablemente llave duplicada
        }
    }

    override suspend fun removeFollower(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.deleteWhere { 
            (TournamentFollowersTable.userId eq userId) and 
            (TournamentFollowersTable.tournamentId eq tournamentId) 
        } > 0
    }

    override suspend fun countFollowers(tournamentId: UUID): Long = dbQuery {
        TournamentFollowersTable.selectAll().where { TournamentFollowersTable.tournamentId eq tournamentId }
            .count()
    }

    override suspend fun isFollowing(userId: UUID, tournamentId: UUID): Boolean = dbQuery {
        TournamentFollowersTable.selectAll()
            .where { (TournamentFollowersTable.userId eq userId) and (TournamentFollowersTable.tournamentId eq tournamentId) }
            .count() > 0
    }

    override suspend fun findFollowedByUser(userId: UUID): List<Tournament> = dbQuery {
        (TournamentsTable innerJoin TournamentFollowersTable)
            .selectAll().where { TournamentFollowersTable.userId eq userId }
            .map { it.toTournament() }
    }
}