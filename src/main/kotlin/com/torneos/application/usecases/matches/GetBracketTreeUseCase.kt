package com.torneos.application.usecases.matches

import com.torneos.domain.models.Match
import com.torneos.domain.ports.MatchRepository
import com.torneos.domain.services.BracketNode
import com.torneos.domain.services.BracketService
import java.util.UUID

/**
 * Caso de uso para visualizar el bracket de un torneo como árbol binario
 * Demuestra el uso de ÁRBOL como estructura de datos
 */
class GetBracketTreeUseCase(
    private val matchRepository: MatchRepository,
    private val bracketService: BracketService
) {

    data class BracketTreeResult(
        val tree: Map<String, Any?>,
        val depth: Int,
        val totalMatches: Int,
        val matchesInOrder: List<Match>
    )

    suspend fun execute(tournamentId: UUID): BracketTreeResult {
        // Obtener todos los partidos del torneo
        val matches = matchRepository.findByTournament(tournamentId)

        // Filtrar solo partidos de eliminación directa (sin groupId)
        val eliminationMatches = matches.filter { it.groupId == null && it.roundNumber != null }

        if (eliminationMatches.isEmpty()) {
            throw IllegalArgumentException("No hay partidos de eliminación directa en este torneo")
        }

        // Construir el árbol binario
        val bracketTree = bracketService.buildBracketTree(eliminationMatches)
            ?: throw IllegalStateException("No se pudo construir el árbol del bracket")

        // Obtener información del árbol
        val depth = bracketService.getDepth(bracketTree)
        val inorderMatches = bracketService.inorderTraversal(bracketTree)
        val serializedTree = bracketService.serializeTree(bracketTree)

        return BracketTreeResult(
            tree = serializedTree,
            depth = depth,
            totalMatches = eliminationMatches.size,
            matchesInOrder = inorderMatches
        )
    }

    /**
     * Valida si un partido específico está en el árbol del bracket
     */
    suspend fun validateMatchInBracket(tournamentId: UUID, matchId: UUID): Boolean {
        val matches = matchRepository.findByTournament(tournamentId)
        val eliminationMatches = matches.filter { it.groupId == null && it.roundNumber != null }
        
        val bracketTree = bracketService.buildBracketTree(eliminationMatches) ?: return false
        return bracketService.validatePathFromFinal(bracketTree, matchId)
    }

    /**
     * Obtiene la ruta desde la final hasta un partido específico
     */
    suspend fun getPathToFinal(tournamentId: UUID, matchId: UUID): List<Match> {
        val matches = matchRepository.findByTournament(tournamentId)
        val eliminationMatches = matches.filter { it.groupId == null && it.roundNumber != null }
        
        val bracketTree = bracketService.buildBracketTree(eliminationMatches)
            ?: throw IllegalStateException("No se pudo construir el árbol del bracket")
        
        val path = bracketService.getPathToMatch(bracketTree, matchId)
        if (path.isEmpty()) {
            throw IllegalArgumentException("El partido no existe en el bracket de este torneo")
        }
        
        return path
    }
}
