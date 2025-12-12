package com.torneos.application.usecases.matches

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.enums.UserRole
import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.ports.TournamentRepository
import com.torneos.domain.ports.UserRepository
import com.torneos.domain.services.MatchHistoryStack
import java.time.Instant
import java.util.UUID


class UpdateMatchResultWithSetsUseCase(
    private val matchRepository: MatchRepository,
    private val standingRepository: StandingRepository,
    private val userRepository: UserRepository,
    private val tournamentRepository: TournamentRepository
) {

    data class SetScore(
        val homeScore: Int,
        val awayScore: Int
    )

    suspend fun execute(
        matchId: UUID,
        userId: UUID,
        sets: List<SetScore>,
        status: MatchStatus = MatchStatus.finished,
        winnerId: UUID? = null
    ): Match {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")

        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")

        val tournament = tournamentRepository.findById(match.tournamentId)
            ?: throw NoSuchElementException("Torneo no encontrado")

        when (user.role) {
            UserRole.admin -> {}
            UserRole.organizer -> {
                if (tournament.organizerId != userId) {
                    throw SecurityException("Solo el organizador del torneo puede actualizar este partido")
                }
            }
            UserRole.referee -> {
                if (match.refereeId != userId) {
                    throw SecurityException("No tienes permiso para arbitrar este partido")
                }
            }
            else -> {
                throw SecurityException("No tienes permiso para actualizar resultados")
            }
        }

        if (sets.isEmpty()) {
            throw IllegalArgumentException("Debe proporcionar al menos un set")
        }


        val homeSets = sets.map { it.homeScore }
        val awaySets = sets.map { it.awayScore }


        var homeSetsWon = 0
        var awaySetsWon = 0

        for (i in sets.indices) {
            when {
                sets[i].homeScore > sets[i].awayScore -> homeSetsWon++
                sets[i].awayScore > sets[i].homeScore -> awaySetsWon++
            }
        }


        val finalWinnerId = winnerId ?: when {
            homeSetsWon > awaySetsWon -> match.teamHomeId
            awaySetsWon > homeSetsWon -> match.teamAwayId
            else -> null
        }


        MatchHistoryStack.push(match, userId)


        val updatedMatch = match.copy(
            scoreHome = homeSetsWon,
            scoreAway = awaySetsWon,
            homeSets = homeSets,
            awaySets = awaySets,
            status = status,
            winnerId = finalWinnerId,
            finishedAt = if (status == MatchStatus.finished) Instant.now() else match.finishedAt,
            updatedAt = Instant.now()
        )


        if (!updatedMatch.validateSets()) {
            throw IllegalArgumentException("Los sets proporcionados no son válidos")
        }

        val savedMatch = matchRepository.update(updatedMatch)
            ?: throw IllegalStateException("Error al guardar el partido")


        if (savedMatch.groupId != null && savedMatch.status == MatchStatus.finished) {
            standingRepository.updateStandings(savedMatch.groupId)
            

            com.torneos.domain.services.StandingsCache.invalidate(savedMatch.groupId)
        }

        return savedMatch
    }


    suspend fun addSet(
        matchId: UUID,
        userId: UUID,
        homeScore: Int,
        awayScore: Int
    ): Match {
        val match = matchRepository.findById(matchId)
            ?: throw NoSuchElementException("Partido no encontrado")


        val newHomeSets = match.homeSets + homeScore
        val newAwaySets = match.awaySets + awayScore


        var homeSetsWon = 0
        var awaySetsWon = 0

        for (i in newHomeSets.indices) {
            when {
                newHomeSets[i] > newAwaySets[i] -> homeSetsWon++
                newAwaySets[i] > newHomeSets[i] -> awaySetsWon++
            }
        }


        MatchHistoryStack.push(match, userId)

        val updatedMatch = match.copy(
            homeSets = newHomeSets,
            awaySets = newAwaySets,
            scoreHome = homeSetsWon,
            scoreAway = awaySetsWon,
            updatedAt = Instant.now()
        )

        return matchRepository.update(updatedMatch)
            ?: throw IllegalStateException("Error al actualizar el partido")
    }
}
