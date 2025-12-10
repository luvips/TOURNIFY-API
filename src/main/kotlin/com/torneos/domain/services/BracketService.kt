package com.torneos.domain.services

import com.torneos.domain.models.Match
import java.util.UUID

/**
 * Nodo del árbol binario que representa un partido en el bracket
 */
data class BracketNode(
    val match: Match,
    val leftChild: BracketNode? = null,  // Partido previo izquierdo
    val rightChild: BracketNode? = null, // Partido previo derecho
    val level: Int = 0                    // Nivel en el árbol (0 = final, 1 = semifinal, etc.)
)

/**
 * Servicio de dominio que estructura partidos de eliminación directa
 * como un Árbol Binario para visualizar el bracket y validar rutas.
 * 
 * Uso de Estructura de Datos: ÁRBOL BINARIO
 * - Cada nodo es un partido
 * - Los hijos son los partidos previos que alimentan ese partido
 * - La raíz es la final
 */
class BracketService {

    /**
     * Construye un árbol binario desde los partidos de eliminación directa.
     * Asume que los partidos están ordenados por roundNumber (mayor = final)
     */
    fun buildBracketTree(matches: List<Match>): BracketNode? {
        if (matches.isEmpty()) return null

        // Agrupar partidos por ronda
        val matchesByRound = matches
            .filter { it.roundNumber != null }
            .sortedByDescending { it.roundNumber }
            .groupBy { it.roundNumber!! }

        // La ronda más alta es la final (raíz del árbol)
        val finalRound = matchesByRound.keys.maxOrNull() ?: return null
        val finalMatch = matchesByRound[finalRound]?.firstOrNull() ?: return null

        // Construir el árbol recursivamente desde la final
        return buildNodeRecursively(finalMatch, matchesByRound, finalRound)
    }

    private fun buildNodeRecursively(
        match: Match,
        matchesByRound: Map<Int, List<Match>>,
        currentRound: Int
    ): BracketNode {
        val level = (matchesByRound.keys.maxOrNull() ?: currentRound) - currentRound

        // Si es la primera ronda, no hay hijos
        if (currentRound == 1) {
            return BracketNode(match = match, level = level)
        }

        // Buscar los partidos previos que alimentan este partido
        val previousRound = currentRound - 1
        val previousMatches = matchesByRound[previousRound] ?: emptyList()

        // Determinar qué partidos previos corresponden a este partido
        // Asumimos que matchNumber indica la conexión (simplificación)
        val matchNumber = match.matchNumber ?: 0
        val leftChildMatch = previousMatches.getOrNull((matchNumber - 1) * 2)
        val rightChildMatch = previousMatches.getOrNull((matchNumber - 1) * 2 + 1)

        val leftChild = leftChildMatch?.let { buildNodeRecursively(it, matchesByRound, previousRound) }
        val rightChild = rightChildMatch?.let { buildNodeRecursively(it, matchesByRound, previousRound) }

        return BracketNode(
            match = match,
            leftChild = leftChild,
            rightChild = rightChild,
            level = level
        )
    }

    /**
     * Valida la ruta desde la final hasta un partido inicial
     * Retorna true si existe un camino válido en el árbol
     */
    fun validatePathFromFinal(root: BracketNode?, targetMatchId: UUID): Boolean {
        if (root == null) return false
        if (root.match.id == targetMatchId) return true

        return validatePathFromFinal(root.leftChild, targetMatchId) ||
               validatePathFromFinal(root.rightChild, targetMatchId)
    }

    /**
     * Obtiene la ruta desde la raíz (final) hasta un partido específico
     */
    fun getPathToMatch(root: BracketNode?, targetMatchId: UUID): List<Match> {
        if (root == null) return emptyList()
        if (root.match.id == targetMatchId) return listOf(root.match)

        val leftPath = getPathToMatch(root.leftChild, targetMatchId)
        if (leftPath.isNotEmpty()) return listOf(root.match) + leftPath

        val rightPath = getPathToMatch(root.rightChild, targetMatchId)
        if (rightPath.isNotEmpty()) return listOf(root.match) + rightPath

        return emptyList()
    }

    /**
     * Recorrido en orden (inorder) del árbol para listar partidos
     */
    fun inorderTraversal(node: BracketNode?): List<Match> {
        if (node == null) return emptyList()

        val result = mutableListOf<Match>()
        result.addAll(inorderTraversal(node.leftChild))
        result.add(node.match)
        result.addAll(inorderTraversal(node.rightChild))
        return result
    }

    /**
     * Obtiene la profundidad del árbol (número de rondas)
     */
    fun getDepth(node: BracketNode?): Int {
        if (node == null) return 0
        val leftDepth = getDepth(node.leftChild)
        val rightDepth = getDepth(node.rightChild)
        return 1 + maxOf(leftDepth, rightDepth)
    }

    /**
     * Serializa el árbol a una estructura anidada para JSON
     */
    fun serializeTree(node: BracketNode?): Map<String, Any?> {
        if (node == null) return emptyMap()

        return mapOf(
            "match" to mapOf(
                "id" to node.match.id.toString(),
                "roundName" to node.match.roundName,
                "roundNumber" to node.match.roundNumber,
                "matchNumber" to node.match.matchNumber,
                "teamHomeId" to node.match.teamHomeId?.toString(),
                "teamAwayId" to node.match.teamAwayId?.toString(),
                "scoreHome" to node.match.scoreHome,
                "scoreAway" to node.match.scoreAway,
                "status" to node.match.status.toString()
            ),
            "level" to node.level,
            "leftChild" to serializeTree(node.leftChild),
            "rightChild" to serializeTree(node.rightChild)
        )
    }
}
