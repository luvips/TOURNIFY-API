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

    // Services
    single<AuthServicePort> { BCryptAuthService(config) }
    single<FileStoragePort> { S3Service(config) }

    // Repositories
    single<UserRepository> { PostgresUserRepository() }
    single<TournamentRepository> { PostgresTournamentRepository() }
    single<TeamRepository> { PostgresTeamRepository() }
    single<SportRepository> { PostgresSportRepository() }
    single<MatchRepository> { PostgresMatchRepository() }
    single<RegistrationRepository> { PostgresRegistrationRepository() }
    single<TournamentGroupRepository> { PostgresTournamentGroupRepository() }
    single<StandingRepository> { PostgresStandingRepository() }


    // Auth
    single { LoginUseCase(get(), get()) }
    single { RegisterUserUseCase(get(), get()) }
    
    // Users
    single { GetUserProfileUseCase(get(),get()) }
    single { UpdateUserProfileUseCase(get()) }
    single { SwitchUserRoleUseCase(get(), get()) }
    single { UpdateUserAvatarUseCase(get(), get()) }
    single { GetUsersByRoleUseCase(get()) }
    
    // Tournaments
    single { CreateTournamentUseCase(get(), get()) }
    single { GetTournamentsUseCase(get(), get()) }
    single { GetTournamentDetailsUseCase(get(), get(), get()) }
    single { GetTournamentStandingsUseCase(get(), get()) } // MatchRepo o StandingRepo
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

    // Teams
    single { CreateTeamUseCase(get(), get()) }
    single { GetMyTeamsUseCase(get()) }
    single { GetTeamDetailsUseCase(get()) }
    single { AddMemberUseCase(get(), get()) }
    single { DeleteTeamUseCase(get()) }
    single { RemoveMemberUseCase(get()) }
    single { UpdateTeamUseCase(get()) }
    
    // Sports
    single { GetSportsUseCase(get()) }
    single { CreateSportUseCase(get()) }
    single { UpdateSportUseCase(get()) }
    single { DeleteSportUseCase(get()) }
    
    // Matches
    single { CreateMatchUseCase(get(), get(), get()) }
    single { GenerateBracketUseCase(get(), get(), get(), get()) }
    single { UpdateMatchResultUseCase(get(), get(), get(), get()) }
    single { GetMatchDetailsUseCase(get(), get(), get(), get()) }
    single { DeleteMatchUseCase(get()) }
    single { GetRefereeMatchesUseCase(get()) }

    //Grupos
    single { GenerateGroupsUseCase(get(), get()) }
    single { AssignTeamsToGroupsUseCase(get(), get()) }
    single { GenerateGroupMatchesUseCase(get(), get(), get()) }

    // ========== NUEVOS USE CASES: ESTRUCTURAS DE DATOS ==========
    
    // Domain Services (Singletons)
    single { BracketService() }
    
    // Árbol (Tree): Visualización de bracket
    single { GetBracketTreeUseCase(get(), get()) }
    
    // Cola (Queue): Lista de espera de equipos
    single { GetWaitingQueueUseCase() }
    single { WithdrawFromTournamentUseCase(get(), get()) }
    
    // Pila (Stack): Deshacer resultados
    single { UndoMatchResultUseCase(get(), get(), get(), get()) }
    
    // Arrays: Marcador por sets
    single { UpdateMatchResultWithSetsUseCase(get(), get(), get(), get()) }
    
    // Conjunto (Set): Validación de jugadores únicos
    single { ValidateUniquePlayersUseCase(get(), get()) }
    
    // Diccionario (Map): Caché de standings
    single { GetCachedStandingsUseCase(get()) }
}