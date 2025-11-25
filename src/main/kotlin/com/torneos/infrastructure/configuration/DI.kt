package com.torneos.infrastructure.configuration

import com.torneos.application.usecases.auth.*
import com.torneos.application.usecases.matches.*
import com.torneos.application.usecases.sports.*
import com.torneos.application.usecases.teams.*
import com.torneos.application.usecases.tournaments.*
import com.torneos.application.usecases.users.*
import com.torneos.domain.ports.*
import com.torneos.infrastructure.adapters.output.persistence.repositories.*
import com.torneos.infrastructure.adapters.output.services.BCryptAuthService
import com.torneos.infrastructure.adapters.output.services.S3Service
import org.koin.dsl.module

val appModule = module {
    // --- INFRASTRUCTURE (Singletons) ---

    // Services
    single<AuthServicePort> { BCryptAuthService(get()) } // Requiere config para JWT secret
    single { S3Service(get()) } // Inyectamos ApplicationConfig

    // Repositories
    single<UserRepository> { PostgresUserRepository() }
    single<TournamentRepository> { PostgresTournamentRepository() }
    single<TeamRepository> { PostgresTeamRepository() }
    single<SportRepository> { PostgresSportRepository() } // Asegúrate de crear este repo
    single<MatchRepository> { PostgresMatchRepository() }
    single<RegistrationRepository> { PostgresRegistrationRepository() } // Asegúrate de crear este repo
    // single<StandingRepository> { PostgresStandingRepository() } // Si lo separaste

    // --- APPLICATION (Use Cases - Factory/Single) ---

    // Auth
    single { LoginUseCase(get(), get()) }
    single { RegisterUserUseCase(get(), get()) }

    // Users
    single { GetUserProfileUseCase(get()) }
    single { UpdateUserProfileUseCase(get()) }

    // Tournaments
    single { CreateTournamentUseCase(get()) }
    single { GetTournamentsUseCase(get()) }
    single { GetTournamentDetailsUseCase(get()) }
    single { GetTournamentStandingsUseCase(get(), get()) } // MatchRepo o StandingRepo
    single { GetTournamentMatchesUseCase(get()) }
    single { GetTournamentTeamsUseCase(get(), get()) }
    single { JoinTournamentUseCase(get(), get(), get()) }
    single { FollowTournamentUseCase(get()) }
    single { UnfollowTournamentUseCase(get()) }

    // Teams
    single { CreateTeamUseCase(get()) }
    single { GetMyTeamsUseCase(get()) }
    single { AddMemberUseCase(get()) }

    // Sports
    single { GetSportsUseCase(get()) }
    single { CreateSportUseCase(get()) }

    // Matches
    single { UpdateMatchResultUseCase(get()) }
    single { GetMatchDetailsUseCase(get()) }
}