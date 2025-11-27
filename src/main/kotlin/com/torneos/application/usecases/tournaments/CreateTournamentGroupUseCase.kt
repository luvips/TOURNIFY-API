package com.torneos.application.usecases.tournaments

import com.torneos.domain.models.TournamentGroup
import com.torneos.domain.ports.TournamentGroupRepository
import java.util.UUID

class CreateTournamentGroupUseCase(
    private val groupRepository: TournamentGroupRepository
) {
    suspend fun execute(tournamentId: UUID, groupName: String, displayOrder: Int): TournamentGroup {
        val group = TournamentGroup(
            tournamentId = tournamentId,
            groupName = groupName,
            displayOrder = displayOrder
        )
        return groupRepository.create(group)
    }
}