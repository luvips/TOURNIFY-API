package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Match
import com.torneos.infrastructure.adapters.input.dtos.MatchResponse

fun Match.toResponse(): MatchResponse {

    return MatchResponse(
        id = this.id.toString(),
        homeTeamName = this.teamHomeId?.toString() ?: "TBD",
        awayTeamName = this.teamAwayId?.toString() ?: "TBD",
        scoreHome = this.scoreHome,
        scoreAway = this.scoreAway,
        status = this.status,
        scheduledDate = this.scheduledDate?.toString()
    )
}
