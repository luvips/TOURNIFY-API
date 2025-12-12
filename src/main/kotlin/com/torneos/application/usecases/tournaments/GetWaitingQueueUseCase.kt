package com.torneos.application.usecases.tournaments

import com.torneos.domain.services.TournamentWaitingQueueService
import com.torneos.domain.services.WaitingQueueEntry
import java.util.UUID


class GetWaitingQueueUseCase {

    data class WaitingQueueInfo(
        val tournamentId: UUID,
        val queueSize: Int,
        val entries: List<QueueEntryInfo>
    )

    data class QueueEntryInfo(
        val teamId: UUID,
        val userId: UUID,
        val position: Int,
        val enqueuedAt: Long,
        val waitingTimeMinutes: Long
    )

    fun execute(tournamentId: UUID): WaitingQueueInfo {
        val queue = TournamentWaitingQueueService.getQueue(tournamentId)
        val size = TournamentWaitingQueueService.getQueueSize(tournamentId)

        val currentTime = System.currentTimeMillis()
        val entries = queue.mapIndexed { index, entry ->
            QueueEntryInfo(
                teamId = entry.teamId,
                userId = entry.userId,
                position = index + 1,
                enqueuedAt = entry.enqueuedAt,
                waitingTimeMinutes = (currentTime - entry.enqueuedAt) / (1000 * 60)
            )
        }

        return WaitingQueueInfo(
            tournamentId = tournamentId,
            queueSize = size,
            entries = entries
        )
    }


    fun getNext(tournamentId: UUID): WaitingQueueEntry? {
        return TournamentWaitingQueueService.peek(tournamentId)
    }


    fun getTeamPosition(tournamentId: UUID, teamId: UUID): Int {
        return TournamentWaitingQueueService.getPosition(tournamentId, teamId)
    }
}
