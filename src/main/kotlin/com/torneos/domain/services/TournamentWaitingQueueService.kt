package com.torneos.domain.services

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.ArrayDeque


data class WaitingQueueEntry(
    val teamId: UUID,
    val userId: UUID,
    val enqueuedAt: Long = System.currentTimeMillis()
)


object TournamentWaitingQueueService {
    

    private val waitingQueues: ConcurrentHashMap<UUID, ArrayDeque<WaitingQueueEntry>> = ConcurrentHashMap()


    fun enqueue(tournamentId: UUID, teamId: UUID, userId: UUID): Boolean {
        val queue = waitingQueues.getOrPut(tournamentId) { ArrayDeque() }
        

        if (queue.any { it.teamId == teamId }) {
            return false
        }
        
        val entry = WaitingQueueEntry(teamId, userId)
        queue.addLast(entry)
        return true
    }


    fun dequeue(tournamentId: UUID): WaitingQueueEntry? {
        val queue = waitingQueues[tournamentId] ?: return null
        
        return if (queue.isNotEmpty()) {
            queue.removeFirst()
        } else {
            null
        }
    }


    fun peek(tournamentId: UUID): WaitingQueueEntry? {
        val queue = waitingQueues[tournamentId] ?: return null
        return queue.firstOrNull()
    }


    fun getQueue(tournamentId: UUID): List<WaitingQueueEntry> {
        val queue = waitingQueues[tournamentId] ?: return emptyList()
        return queue.toList()
    }


    fun getQueueSize(tournamentId: UUID): Int {
        val queue = waitingQueues[tournamentId] ?: return 0
        return queue.size
    }


    fun isInQueue(tournamentId: UUID, teamId: UUID): Boolean {
        val queue = waitingQueues[tournamentId] ?: return false
        return queue.any { it.teamId == teamId }
    }


    fun removeFromQueue(tournamentId: UUID, teamId: UUID): Boolean {
        val queue = waitingQueues[tournamentId] ?: return false
        return queue.removeIf { it.teamId == teamId }
    }


    fun clearQueue(tournamentId: UUID) {
        waitingQueues.remove(tournamentId)
    }


    fun getPosition(tournamentId: UUID, teamId: UUID): Int {
        val queue = waitingQueues[tournamentId] ?: return -1
        val index = queue.indexOfFirst { it.teamId == teamId }
        return if (index >= 0) index + 1 else -1
    }

    fun getAllQueuesStats(): Map<UUID, Int> {
        return waitingQueues.mapValues { it.value.size }
    }
}
