

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
        val matches = matchRepository.findByTournament(tournamentId)

        val eliminationMatches = matches.filter { it.groupId == null && it.roundNumber != null }

        if (eliminationMatches.isEmpty()) {
            throw IllegalArgumentException("No hay partidos de eliminación directa en este torneo")
        }

        val bracketTree = bracketService.buildBracketTree(eliminationMatches)
            ?: throw IllegalStateException("No se pudo construir el árbol del bracket")

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

    suspend fun validateMatchInBracket(tournamentId: UUID, matchId: UUID): Boolean {
        val matches = matchRepository.findByTournament(tournamentId)
        val eliminationMatches = matches.filter { it.groupId == null && it.roundNumber != null }
        
        val bracketTree = bracketService.buildBracketTree(eliminationMatches) ?: return false
        return bracketService.validatePathFromFinal(bracketTree, matchId)
    }

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
