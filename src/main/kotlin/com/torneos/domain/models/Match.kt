package com.torneos.domain.models

import com.torneos.domain.enums.MatchStatus
import java.time.Instant
import java.util.UUID

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
    
    val matchDataJson: String = "{}",
    val notes: String?,
    
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    

    val homeSets: List<Int> = emptyList(),
    val awaySets: List<Int> = emptyList()
) {

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

    fun validateSets(): Boolean {
        if (homeSets.isEmpty() && awaySets.isEmpty()) return true
        if (homeSets.size != awaySets.size) return false
        return homeSets.all { it >= 0 } && awaySets.all { it >= 0 }
    }
}