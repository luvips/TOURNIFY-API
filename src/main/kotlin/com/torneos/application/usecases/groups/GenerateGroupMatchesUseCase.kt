package com.torneos.application.usecases.groups

import com.torneos.domain.enums.MatchStatus
import com.torneos.domain.models.Match
import com.torneos.domain.ports.*
import java.time.Instant
import java.util.UUID

class GenerateGroupMatchesUseCase(
    private val matchRepository: MatchRepository,
    private val groupRepository: TournamentGroupRepository,
    private val registrationRepository: RegistrationRepository
) {
    suspend fun execute(tournamentId: UUID) {
        val groups = groupRepository.findByTournament(tournamentId)
        val registrations = registrationRepository.findByTournamentId(tournamentId)

        groups.forEach { group ->
            // Filtrar equipos de este grupo
            val groupTeams = registrations
                .filter { it.groupId == group.id }
                .map { it.teamId }

            generateRoundRobin(groupTeams, tournamentId, group.id)
        }
    }

    private suspend fun generateRoundRobin(teams: List<UUID>, tournamentId: UUID, groupId: UUID) {
        val numTeams = teams.size
        if (numTeams < 2) return

        // Algoritmo simple de todos contra todos (ida)
        for (i in 0 until numTeams) {
            for (j in i + 1 until numTeams) {
                val match = Match(
                    id = UUID.randomUUID(),
                    tournamentId = tournamentId,
                    groupId = groupId,
                    teamHomeId = teams[i],
                    teamAwayId = teams[j],
                    matchNumber = null,
                    roundName = "Fase de Grupos",
                    roundNumber = 1,
                    scheduledDate = null,
                    location = null,
                    refereeId = null,
                    status = MatchStatus.scheduled,
                    scoreHome = null, scoreAway = null, winnerId = null,
                    notes = null, startedAt = null, finishedAt = null
                )
                matchRepository.create(match)
            }
        }
    }
}