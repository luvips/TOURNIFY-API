package com.torneos.application.usecases.standings

import com.torneos.domain.models.GroupStanding
import com.torneos.domain.ports.StandingRepository
import com.torneos.domain.services.StandingsCache
import java.util.UUID

/**
 * Caso de uso para obtener la tabla de posiciones con caché.
 * Demuestra el uso de DICCIONARIO (Map) para optimizar lecturas frecuentes.
 */
class GetCachedStandingsUseCase(
    private val standingRepository: StandingRepository
) {

    data class StandingsResult(
        val standings: List<GroupStanding>,
        val fromCache: Boolean,
        val cacheAgeSeconds: Long? = null
    )

    /**
     * Obtener standings con caché automático
     */
    suspend fun execute(groupId: UUID): StandingsResult {
        // Intentar obtener del caché primero (USO DE MAP)
        val cachedStandings = StandingsCache.get(groupId)

        if (cachedStandings != null) {
            val cacheInfo = StandingsCache.getCacheInfo(groupId)
            val cacheAge = cacheInfo?.get("cacheAgeSeconds") as? Long

            return StandingsResult(
                standings = cachedStandings,
                fromCache = true,
                cacheAgeSeconds = cacheAge
            )
        }

        // Si no está en caché, consultar la base de datos
        val standings = standingRepository.getStandingsByGroup(groupId)

        // Guardar en caché para futuras consultas
        StandingsCache.put(groupId, standings)

        return StandingsResult(
            standings = standings,
            fromCache = false,
            cacheAgeSeconds = null
        )
    }

    /**
     * Obtener standings forzando recarga (ignorando caché)
     */
    suspend fun executeWithRefresh(groupId: UUID): StandingsResult {
        // Invalidar caché existente
        StandingsCache.invalidate(groupId)

        // Consultar base de datos
        val standings = standingRepository.getStandingsByGroup(groupId)

        // Actualizar caché
        StandingsCache.put(groupId, standings)

        return StandingsResult(
            standings = standings,
            fromCache = false,
            cacheAgeSeconds = null
        )
    }

    /**
     * Obtener standings de múltiples grupos (batch)
     */
    suspend fun executeForMultipleGroups(groupIds: List<UUID>): Map<UUID, StandingsResult> {
        val results = mutableMapOf<UUID, StandingsResult>()

        for (groupId in groupIds) {
            results[groupId] = execute(groupId)
        }

        return results
    }

    /**
     * Pre-cargar caché para múltiples grupos
     */
    suspend fun warmupCache(groupIds: List<UUID>) {
        for (groupId in groupIds) {
            if (!StandingsCache.exists(groupId)) {
                val standings = standingRepository.getStandingsByGroup(groupId)
                StandingsCache.put(groupId, standings)
            }
        }
    }
}
