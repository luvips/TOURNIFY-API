package com.torneos.domain.services

import com.torneos.domain.models.Match
import java.util.UUID


data class BracketNode(
    val match: Match,
    val leftChild: BracketNode? = null,
    val rightChild: BracketNode? = null,
    val level: Int = 0
)

class BracketService {


    fun buildBracketTree(matches: List<Match>): BracketNode? {
        if (matches.isEmpty()) return null

        val matchesByRound = matches
            .filter { it.roundNumber != null }
            .sortedByDescending { it.roundNumber }
            .groupBy { it.roundNumber!! }

        val finalRound = matchesByRound.keys.maxOrNull() ?: return null
        val finalMatch = matchesByRound[finalRound]?.firstOrNull() ?: return null

        return buildNodeRecursively(finalMatch, matchesByRound, finalRound)
    }

    private fun buildNodeRecursively(
        match: Match,
        matchesByRound: Map<Int, List<Match>>,
        currentRound: Int
    ): BracketNode {
        val level = (matchesByRound.keys.maxOrNull() ?: currentRound) - currentRound


        if (currentRound == 1) {
            return BracketNode(match = match, level = level)
        }


        val previousRound = currentRound - 1
        val previousMatches = matchesByRound[previousRound] ?: emptyList()


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


    fun validatePathFromFinal(root: BracketNode?, targetMatchId: UUID): Boolean {
        if (root == null) return false
        if (root.match.id == targetMatchId) return true

        return validatePathFromFinal(root.leftChild, targetMatchId) ||
               validatePathFromFinal(root.rightChild, targetMatchId)
    }

    fun getPathToMatch(root: BracketNode?, targetMatchId: UUID): List<Match> {
        if (root == null) return emptyList()
        if (root.match.id == targetMatchId) return listOf(root.match)

        val leftPath = getPathToMatch(root.leftChild, targetMatchId)
        if (leftPath.isNotEmpty()) return listOf(root.match) + leftPath

        val rightPath = getPathToMatch(root.rightChild, targetMatchId)
        if (rightPath.isNotEmpty()) return listOf(root.match) + rightPath

        return emptyList()
    }


    fun inorderTraversal(node: BracketNode?): List<Match> {
        if (node == null) return emptyList()

        val result = mutableListOf<Match>()
        result.addAll(inorderTraversal(node.leftChild))
        result.add(node.match)
        result.addAll(inorderTraversal(node.rightChild))
        return result
    }


    fun getDepth(node: BracketNode?): Int {
        if (node == null) return 0
        val leftDepth = getDepth(node.leftChild)
        val rightDepth = getDepth(node.rightChild)
        return 1 + maxOf(leftDepth, rightDepth)
    }

   
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
