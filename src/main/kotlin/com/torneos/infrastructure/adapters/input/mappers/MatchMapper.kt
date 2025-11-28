package com.torneos.infrastructure.adapters.input.mappers

import com.torneos.domain.models.Match
import com.torneos.application.usecases.tournaments.MatchWithTeamNames
import com.torneos.application.usecases.matches.MatchDetails
import com.torneos.infrastructure.adapters.input.dtos.MatchResponse
import com.torneos.infrastructure.adapters.input.dtos.MatchDetailResponse
import com.torneos.infrastructure.adapters.input.dtos.RefereeInfo

fun Match.toResponse(): MatchResponse {

    return MatchResponse(
        id = this.id.toString(),
        homeTeamName = this.teamHomeId?.toString() ?: "TBD",
        awayTeamName = this.teamAwayId?.toString() ?: "TBD",
        scoreHome = this.scoreHome,
        scoreAway = this.scoreAway,
        status = this.status,
        scheduledDate = this.scheduledDate?.toString(),
        refereeId = this.refereeId?.toString()
    )
}

fun MatchWithTeamNames.toResponse(): MatchResponse {
    return MatchResponse(
        id = this.match.id.toString(),
        homeTeamName = this.homeTeamName ?: "TBD",
        awayTeamName = this.awayTeamName ?: "TBD",
        scoreHome = this.match.scoreHome,
        scoreAway = this.match.scoreAway,
        status = this.match.status,
        scheduledDate = this.match.scheduledDate?.toString(),
        refereeId = this.match.refereeId?.toString()
    )
}

fun MatchDetails.toDetailResponse(): MatchDetailResponse {
    return MatchDetailResponse(
        id = this.match.id.toString(),
        homeTeamName = this.homeTeamName ?: "TBD",
        awayTeamName = this.awayTeamName ?: "TBD",
        scoreHome = this.match.scoreHome,
        scoreAway = this.match.scoreAway,
        status = this.match.status,
        scheduledDate = this.match.scheduledDate?.toString(),
        tournamentName = this.tournamentName,
        location = this.match.location,
        roundName = this.match.roundName
    )
}

fun MatchDetails.toResponse(): MatchResponse {
    return MatchResponse(
        id = this.match.id.toString(),
        homeTeamName = this.homeTeamName ?: "TBD",
        awayTeamName = this.awayTeamName ?: "TBD",
        scoreHome = this.match.scoreHome,
        scoreAway = this.match.scoreAway,
        status = this.match.status,
        scheduledDate = this.match.scheduledDate?.toString(),
        tournamentName = this.tournamentName,
        location = this.match.location,
        roundName = this.match.roundName,
        refereeId = this.match.refereeId?.toString(),
        referee = this.referee?.let { RefereeInfo(id = it.id.toString(), username = it.username) }
    )
}
