package com.torneos.infrastructure.configuration

import com.torneos.application.usecases.auth.*
import com.torneos.application.usecases.matches.*
import com.torneos.application.usecases.sports.*
import com.torneos.application.usecases.teams.*
import com.torneos.application.usecases.tournaments.*
import com.torneos.application.usecases.users.*
import com.torneos.application.usecases.groups.*
import com.torneos.application.usecases.standings.*
import com.torneos.domain.ports.*
import com.torneos.domain.services.*
import com.torneos.infrastructure.adapters.output.persistence.repositories.*
import com.torneos.infrastructure.adapters.output.services.BCryptAuthService
import com.torneos.infrastructure.adapters.output.services.S3Service
import io.ktor.server.config.*
import org.koin.dsl.module

fun getAppModule(config: ApplicationConfig) = module {

    single<AuthServicePort> { BCryptAuthService(config) }
    single<FileStoragePort> { S3Service(config) }

    single<UserRepository> { PostgresUserRepository() }
    single<TournamentRepository> { PostgresTournamentRepository() }
    single<TeamRepository> { PostgresTeamRepository() }
    single<SportRepository> { PostgresSportRepository() }
    single<MatchRepository> { PostgresMatchRepository() }
    single<RegistrationRepository> { PostgresRegistrationRepository() }
    single<TournamentGroupRepository> { PostgresTournamentGroupRepository() }
    single<StandingRepository> { PostgresStandingRepository() }


    single { LoginUseCase(get(), get()) }
    single { RegisterUserUseCase(get(), get()) }
    
    single { GetUserProfileUseCase(get(),get()) }
    single { UpdateUserProfileUseCase(get()) }
    single { SwitchUserRoleUseCase(get(), get()) }
    single { UpdateUserAvatarUseCase(get(), get()) }
    single { GetUsersByRoleUseCase(get()) }
    
    single { CreateTournamentUseCase(get(), get()) }
    single { GetTournamentsUseCase(get(), get()) }
    single { GetTournamentDetailsUseCase(get(), get(), get()) }
    single { GetTournamentMatchesUseCase(get(), get()) }
    single { GetTournamentTeamsUseCase(get(), get()) }
    single { GetTournamentRegistrationsUseCase(get(), get()) }
    single { ApproveRegistrationUseCase(get(), get()) }
    single { RejectRegistrationUseCase(get()) }
    single { JoinTournamentUseCase(get(), get(), get()) }
    single { FollowTournamentUseCase(get()) }
    single { UnfollowTournamentUseCase(get()) }
    single { GetFollowedTournamentsUseCase(get(), get()) }
    single { GetMyTournamentsUseCase(get(), get()) }
    single { UpdateTournamentUseCase(get()) }
    single { DeleteTournamentUseCase(get()) }
    single { UpdateTournamentImageUseCase(get(), get()) }
    single { StartTournamentUseCase(get()) }
    single { FinishTournamentUseCase(get()) }

    single { CreateTeamUseCase(get(), get()) }
    single { GetMyTeamsUseCase(get()) }
    single { GetTeamDetailsUseCase(get()) }
    single { AddMemberUseCase(get(), get()) }
    single { DeleteTeamUseCase(get()) }
    single { RemoveMemberUseCase(get()) }
    single { UpdateTeamUseCase(get()) }
    
    single { GetSportsUseCase(get()) }
    single { CreateSportUseCase(get()) }
    single { UpdateSportUseCase(get()) }
    single { DeleteSportUseCase(get()) }
    
    single { CreateMatchUseCase(get(), get(), get()) }
    single { GenerateBracketUseCase(get(), get(), get(), get()) }
    single { UpdateMatchResultUseCase(get(), get(), get(), get()) }
    single { GetMatchDetailsUseCase(get(), get(), get(), get()) }
    single { DeleteMatchUseCase(get()) }
    single { GetRefereeMatchesUseCase(get()) }

    single { GenerateGroupsUseCase(get(), get()) }
    single { AssignTeamsToGroupsUseCase(get(), get()) }
    single { GenerateGroupMatchesUseCase(get(), get(), get()) }

    
    single { BracketService() }
    
    single { GetBracketTreeUseCase(get(), get()) }
    
    single { GetWaitingQueueUseCase() }
    single { WithdrawFromTournamentUseCase(get(), get()) }
    
    single { UndoMatchResultUseCase(get(), get(), get(), get()) }
    
    single { UpdateMatchResultWithSetsUseCase(get(), get(), get(), get()) }
    
    single { ValidateUniquePlayersUseCase(get(), get()) }
    
    single { GetCachedStandingsUseCase(get()) }
    single { GetCachedStandingsUseCase(get()) }
}