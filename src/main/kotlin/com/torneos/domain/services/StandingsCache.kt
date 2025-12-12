package com.torneos.domain.services

import com.torneos.domain.models.GroupStanding
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


object StandingsCache {

    private val cache: ConcurrentHashMap<UUID, CacheEntry> = ConcurrentHashMap()

    data class CacheEntry(
        val standings: List<GroupStanding>,
        val timestamp: Long = System.currentTimeMillis(),
        val version: Int = 1
    )


    fun get(groupId: UUID): List<GroupStanding>? {
        val entry = cache[groupId] ?: return null

        val cacheAgeMillis = System.currentTimeMillis() - entry.timestamp
        val maxCacheAge = 5 * 60 * 1000
        
        return if (cacheAgeMillis > maxCacheAge) {
            invalidate(groupId)
            null
        } else {
            entry.standings
        }
    }


    fun put(groupId: UUID, standings: List<GroupStanding>) {
        val entry = CacheEntry(
            standings = standings,
            timestamp = System.currentTimeMillis()
        )
        cache[groupId] = entry
    }

    fun invalidate(groupId: UUID) {
        cache.remove(groupId)
    }


    fun invalidateAll(groupIds: List<UUID>) {
        groupIds.forEach { invalidate(it) }
    }


    fun exists(groupId: UUID): Boolean {
        return cache.containsKey(groupId)
    }


    suspend fun getOrCompute(
        groupId: UUID,
        computeFunction: suspend () -> List<GroupStanding>
    ): List<GroupStanding> {

        get(groupId)?.let { return it }


        val standings = computeFunction()
        put(groupId, standings)
        return standings
    }


    fun clear() {
        cache.clear()
    }


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


    fun getAllCachedGroupIds(): Set<UUID> {
        return cache.keys.toSet()
    }

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


    fun update(groupId: UUID, standings: List<GroupStanding>) {
        val currentEntry = cache[groupId]
        val newVersion = (currentEntry?.version ?: 0) + 1
        
        cache[groupId] = CacheEntry(
            standings = standings,
            timestamp = System.currentTimeMillis(),
            version = newVersion
        )
    }


    fun cleanOldEntries(maxAgeMinutes: Int = 10) {
        val now = System.currentTimeMillis()
        val maxAgeMillis = maxAgeMinutes * 60 * 1000L

        val toRemove = cache.entries
            .filter { (now - it.value.timestamp) > maxAgeMillis }
            .map { it.key }

        toRemove.forEach { cache.remove(it) }
    }
}
