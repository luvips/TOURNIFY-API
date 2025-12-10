package com.torneos.domain.services

import com.torneos.domain.models.GroupStanding
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Servicio singleton para cachear tabla de posiciones (standings) de grupos.
 * 
 * Uso de Estructura de Datos: DICCIONARIO/MAPA (Map)
 * - Key: UUID del grupo
 * - Value: Lista de GroupStanding (tabla de posiciones)
 * - Optimiza lecturas frecuentes de standings
 * - Se invalida cuando se actualiza un partido del grupo
 */
object StandingsCache {

    // Mapa principal: groupId -> Lista de standings
    private val cache: ConcurrentHashMap<UUID, CacheEntry> = ConcurrentHashMap()

    data class CacheEntry(
        val standings: List<GroupStanding>,
        val timestamp: Long = System.currentTimeMillis(),
        val version: Int = 1
    )

    /**
     * Obtener standings del caché
     * Retorna null si no existe o está invalidado
     */
    fun get(groupId: UUID): List<GroupStanding>? {
        val entry = cache[groupId] ?: return null
        
        // Opcional: verificar si el caché es muy antiguo (ej: más de 5 minutos)
        val cacheAgeMillis = System.currentTimeMillis() - entry.timestamp
        val maxCacheAge = 5 * 60 * 1000 // 5 minutos
        
        return if (cacheAgeMillis > maxCacheAge) {
            invalidate(groupId)
            null
        } else {
            entry.standings
        }
    }

    /**
     * Guardar standings en el caché
     */
    fun put(groupId: UUID, standings: List<GroupStanding>) {
        val entry = CacheEntry(
            standings = standings,
            timestamp = System.currentTimeMillis()
        )
        cache[groupId] = entry
    }

    /**
     * Invalidar el caché de un grupo específico
     * Se llama cuando se actualiza un partido de ese grupo
     */
    fun invalidate(groupId: UUID) {
        cache.remove(groupId)
    }

    /**
     * Invalidar múltiples grupos a la vez
     */
    fun invalidateAll(groupIds: List<UUID>) {
        groupIds.forEach { invalidate(it) }
    }

    /**
     * Verificar si existe caché válido para un grupo
     */
    fun exists(groupId: UUID): Boolean {
        return cache.containsKey(groupId)
    }

    /**
     * Obtener o calcular (útil con función lambda)
     */
    suspend fun getOrCompute(
        groupId: UUID,
        computeFunction: suspend () -> List<GroupStanding>
    ): List<GroupStanding> {
        // Intentar obtener del caché
        get(groupId)?.let { return it }

        // Si no existe, computar y guardar
        val standings = computeFunction()
        put(groupId, standings)
        return standings
    }

    /**
     * Limpiar todo el caché
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Obtener estadísticas del caché
     */
    fun getStats(): Map<String, Any> {
        val entries = cache.values.toList()
        val now = System.currentTimeMillis()
        
        val averageAge = if (entries.isNotEmpty()) {
            entries.map { now - it.timestamp }.average()
        } else {
            0.0
        }

        return mapOf(
            "totalCachedGroups" to cache.size,
            "averageCacheAgeSeconds" to (averageAge / 1000).toInt(),
            "oldestCacheAgeSeconds" to (entries.maxOfOrNull { now - it.timestamp }?.div(1000) ?: 0).toInt(),
            "newestCacheAgeSeconds" to (entries.minOfOrNull { now - it.timestamp }?.div(1000) ?: 0).toInt()
        )
    }

    /**
     * Obtener todas las entradas del caché (para debug)
     */
    fun getAllCachedGroupIds(): Set<UUID> {
        return cache.keys.toSet()
    }

    /**
     * Obtener información detallada de una entrada
     */
    fun getCacheInfo(groupId: UUID): Map<String, Any>? {
        val entry = cache[groupId] ?: return null
        val ageSeconds = (System.currentTimeMillis() - entry.timestamp) / 1000

        return mapOf(
            "groupId" to groupId.toString(),
            "standingsCount" to entry.standings.size,
            "cacheAgeSeconds" to ageSeconds,
            "version" to entry.version,
            "timestamp" to entry.timestamp
        )
    }

    /**
     * Actualizar standings en el caché sin invalidar
     * (útil cuando ya se tienen los datos actualizados)
     */
    fun update(groupId: UUID, standings: List<GroupStanding>) {
        val currentEntry = cache[groupId]
        val newVersion = (currentEntry?.version ?: 0) + 1
        
        cache[groupId] = CacheEntry(
            standings = standings,
            timestamp = System.currentTimeMillis(),
            version = newVersion
        )
    }

    /**
     * Limpiar cachés antiguos (más de N minutos)
     */
    fun cleanOldEntries(maxAgeMinutes: Int = 10) {
        val now = System.currentTimeMillis()
        val maxAgeMillis = maxAgeMinutes * 60 * 1000L

        val toRemove = cache.entries
            .filter { (now - it.value.timestamp) > maxAgeMillis }
            .map { it.key }

        toRemove.forEach { cache.remove(it) }
    }
}
