package com.torneos.domain.services

import com.torneos.domain.models.Match
import java.util.UUID
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap

/**
 * Snapshot del estado de un partido antes de una actualización
 */
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

/**
 * Servicio singleton para gestionar historial de cambios en partidos usando PILA (Stack).
 * 
 * Uso de Estructura de Datos: PILA (Stack)
 * - LIFO: Last In, First Out
 * - Antes de actualizar un partido, se guarda un snapshot en la pila
 * - Para deshacer, se hace pop y se restaura el estado anterior
 * - Cada árbitro/partido tiene su propia pila de cambios
 */
object MatchHistoryStack {

    // Mapa de matchId -> Pila de snapshots
    private val historyStacks: ConcurrentHashMap<UUID, Stack<MatchSnapshot>> = ConcurrentHashMap()

    // Límite de snapshots por partido (para evitar memory leak)
    private const val MAX_HISTORY_SIZE = 10

    /**
     * Guardar el estado actual de un partido antes de modificarlo (PUSH)
     */
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

        // Limitar el tamaño de la pila
        if (stack.size > MAX_HISTORY_SIZE) {
            // Remover el elemento más antiguo (en el fondo de la pila)
            stack.removeAt(0)
        }
    }

    /**
     * Deshacer el último cambio de un partido (POP)
     * Retorna el snapshot anterior o null si no hay historial
     */
    fun pop(matchId: UUID): MatchSnapshot? {
        val stack = historyStacks[matchId] ?: return null

        return if (stack.isNotEmpty()) {
            stack.pop() // Remover y retornar el último elemento (LIFO)
        } else {
            null
        }
    }

    /**
     * Ver el último cambio sin removerlo (PEEK)
     */
    fun peek(matchId: UUID): MatchSnapshot? {
        val stack = historyStacks[matchId] ?: return null
        return if (stack.isNotEmpty()) stack.peek() else null
    }

    /**
     * Obtener todo el historial de un partido (sin modificar la pila)
     */
    fun getHistory(matchId: UUID): List<MatchSnapshot> {
        val stack = historyStacks[matchId] ?: return emptyList()
        return stack.toList()
    }

    /**
     * Verificar si hay historial disponible para deshacer
     */
    fun canUndo(matchId: UUID): Boolean {
        val stack = historyStacks[matchId] ?: return false
        return stack.isNotEmpty()
    }

    /**
     * Obtener el tamaño de la pila de historial
     */
    fun getHistorySize(matchId: UUID): Int {
        val stack = historyStacks[matchId] ?: return 0
        return stack.size
    }

    /**
     * Limpiar el historial de un partido específico
     */
    fun clearHistory(matchId: UUID) {
        historyStacks.remove(matchId)
    }

    /**
     * Limpiar todo el historial (útil para pruebas)
     */
    fun clearAll() {
        historyStacks.clear()
    }

    /**
     * Obtener estadísticas de todos los historiales
     */
    fun getAllHistoryStats(): Map<UUID, Int> {
        return historyStacks.mapValues { it.value.size }
    }
}
