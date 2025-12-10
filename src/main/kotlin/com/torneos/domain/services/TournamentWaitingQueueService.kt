package com.torneos.domain.services

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.ArrayDeque

/**
 * Entrada en la cola de espera de un torneo
 */
data class WaitingQueueEntry(
    val teamId: UUID,
    val userId: UUID,
    val enqueuedAt: Long = System.currentTimeMillis()
)

/**
 * Servicio singleton para gestionar colas de espera de equipos por torneo.
 * 
 * Uso de Estructura de Datos: COLA (Queue con ArrayDeque)
 * - FIFO: First In, First Out
 * - Cuando un torneo está lleno, equipos se agregan a la cola
 * - Cuando un equipo se retira, se desencola el siguiente automáticamente
 * - En memoria (se pierde al reiniciar el servidor)
 */
object TournamentWaitingQueueService {
    
    // Mapa de torneoId -> Cola de equipos en espera
    private val waitingQueues: ConcurrentHashMap<UUID, ArrayDeque<WaitingQueueEntry>> = ConcurrentHashMap()

    /**
     * Encolar un equipo en la lista de espera de un torneo
     */
    fun enqueue(tournamentId: UUID, teamId: UUID, userId: UUID): Boolean {
        val queue = waitingQueues.getOrPut(tournamentId) { ArrayDeque() }
        
        // Verificar que el equipo no esté ya en la cola
        if (queue.any { it.teamId == teamId }) {
            return false
        }
        
        val entry = WaitingQueueEntry(teamId, userId)
        queue.addLast(entry) // Agregar al final de la cola
        return true
    }

    /**
     * Desencolar el siguiente equipo en espera (FIFO)
     * Retorna el equipo desencolado o null si la cola está vacía
     */
    fun dequeue(tournamentId: UUID): WaitingQueueEntry? {
        val queue = waitingQueues[tournamentId] ?: return null
        
        return if (queue.isNotEmpty()) {
            queue.removeFirst() // Remover del inicio de la cola (FIFO)
        } else {
            null
        }
    }

    /**
     * Ver el siguiente equipo sin desencolarlo (peek)
     */
    fun peek(tournamentId: UUID): WaitingQueueEntry? {
        val queue = waitingQueues[tournamentId] ?: return null
        return queue.firstOrNull()
    }

    /**
     * Obtener toda la cola de espera de un torneo
     */
    fun getQueue(tournamentId: UUID): List<WaitingQueueEntry> {
        val queue = waitingQueues[tournamentId] ?: return emptyList()
        return queue.toList()
    }

    /**
     * Obtener el tamaño de la cola de espera
     */
    fun getQueueSize(tournamentId: UUID): Int {
        val queue = waitingQueues[tournamentId] ?: return 0
        return queue.size
    }

    /**
     * Verificar si un equipo está en la cola de espera
     */
    fun isInQueue(tournamentId: UUID, teamId: UUID): Boolean {
        val queue = waitingQueues[tournamentId] ?: return false
        return queue.any { it.teamId == teamId }
    }

    /**
     * Remover un equipo específico de la cola (si cancela su espera)
     */
    fun removeFromQueue(tournamentId: UUID, teamId: UUID): Boolean {
        val queue = waitingQueues[tournamentId] ?: return false
        return queue.removeIf { it.teamId == teamId }
    }

    /**
     * Limpiar toda la cola de un torneo
     */
    fun clearQueue(tournamentId: UUID) {
        waitingQueues.remove(tournamentId)
    }

    /**
     * Obtener posición de un equipo en la cola (1-indexed)
     */
    fun getPosition(tournamentId: UUID, teamId: UUID): Int {
        val queue = waitingQueues[tournamentId] ?: return -1
        val index = queue.indexOfFirst { it.teamId == teamId }
        return if (index >= 0) index + 1 else -1
    }

    /**
     * Estadísticas de todas las colas
     */
    fun getAllQueuesStats(): Map<UUID, Int> {
        return waitingQueues.mapValues { it.value.size }
    }
}
