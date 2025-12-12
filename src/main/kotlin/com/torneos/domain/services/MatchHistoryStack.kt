package com.torneos.domain.services

import com.torneos.domain.models.Match
import java.util.UUID
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap


data class MatchSnapshot(
    val matchId: UUID,
    val scoreHome: Int?,
    val scoreAway: Int?,
    val winnerId: UUID?,
    val status: String,
    val matchDataJson: String,
    val finishedAt: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedBy: UUID
)


object MatchHistoryStack {


    private val historyStacks: ConcurrentHashMap<UUID, Stack<MatchSnapshot>> = ConcurrentHashMap()


    private const val MAX_HISTORY_SIZE = 10

   
    fun push(match: Match, updatedBy: UUID) {
        val stack = historyStacks.getOrPut(match.id) { Stack() }

        val snapshot = MatchSnapshot(
            matchId = match.id,
            scoreHome = match.scoreHome,
            scoreAway = match.scoreAway,
            winnerId = match.winnerId,
            status = match.status.toString(),
            matchDataJson = match.matchDataJson,
            finishedAt = match.finishedAt?.toString(),
            updatedBy = updatedBy
        )

        stack.push(snapshot)


        if (stack.size > MAX_HISTORY_SIZE) {

            stack.removeAt(0)
        }
    }


    fun pop(matchId: UUID): MatchSnapshot? {
        val stack = historyStacks[matchId] ?: return null

        return if (stack.isNotEmpty()) {
            stack.pop()
        } else {
            null
        }
    }


    fun peek(matchId: UUID): MatchSnapshot? {
        val stack = historyStacks[matchId] ?: return null
        return if (stack.isNotEmpty()) stack.peek() else null
    }

    fun getHistory(matchId: UUID): List<MatchSnapshot> {
        val stack = historyStacks[matchId] ?: return emptyList()
        return stack.toList()
    }


    fun canUndo(matchId: UUID): Boolean {
        val stack = historyStacks[matchId] ?: return false
        return stack.isNotEmpty()
    }


    fun getHistorySize(matchId: UUID): Int {
        val stack = historyStacks[matchId] ?: return 0
        return stack.size
    }


    fun clearHistory(matchId: UUID) {
        historyStacks.remove(matchId)
    }


    fun clearAll() {
        historyStacks.clear()
    }


    fun getAllHistoryStats(): Map<UUID, Int> {
        return historyStacks.mapValues { it.value.size }
    }
}
