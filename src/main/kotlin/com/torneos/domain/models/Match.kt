package com.torneos.domain.models

import com.torneos.domain.enums.MatchStatus
import java.time.Instant
import java.util.UUID

/**
 * Modelo de dominio para Match.
 * 
 * Uso de Estructura de Datos: ARREGLOS (Arrays/Lists)
 * - homeSets y awaySets son listas de puntajes por set
 * - Ejemplo para tenis/voley: [6, 4, 6] significa ganó los sets 1 y 3
 * - Estas propiedades NO se persisten como columnas nuevas
 * - Se serializan dentro del JSON matchDataJson existente
 */
data class Match(
    val id: UUID = UUID.randomUUID(),
    val tournamentId: UUID,
    val groupId: UUID?,
    
    val matchNumber: Int?,
    val roundName: String?,
    val roundNumber: Int?,
    
    val teamHomeId: UUID?,
    val teamAwayId: UUID?,
    
    val scheduledDate: Instant?,
    val location: String?,
    val refereeId: UUID?,
    
    val status: MatchStatus,
    val scoreHome: Int?,
    val scoreAway: Int?,
    val winnerId: UUID?,
    
    val matchDataJson: String = "{}", // JSONB - contiene los sets y otros datos
    val notes: String?,
    
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    
    // Propiedades transitorias para manejar sets (ARREGLOS)
    // NO son columnas de BD, se extraen/guardan en matchDataJson
    val homeSets: List<Int> = emptyList(),
    val awaySets: List<Int> = emptyList()
) {
    /**
     * Calcula el marcador total a partir de los sets (sets ganados)
     */
    fun calculateScoreFromSets(): Pair<Int, Int> {
        var homeWins = 0
        var awayWins = 0
        
        for (i in homeSets.indices) {
            if (i < awaySets.size) {
                val homeSetScore = homeSets[i]
                val awaySetScore = awaySets[i]
                
                when {
                    homeSetScore > awaySetScore -> homeWins++
                    awaySetScore > homeSetScore -> awayWins++
                }
            }
        }
        
        return Pair(homeWins, awayWins)
    }
    
    /**
     * Valida que los sets sean consistentes
     */
    fun validateSets(): Boolean {
        if (homeSets.isEmpty() && awaySets.isEmpty()) return true
        if (homeSets.size != awaySets.size) return false
        return homeSets.all { it >= 0 } && awaySets.all { it >= 0 }
    }
}