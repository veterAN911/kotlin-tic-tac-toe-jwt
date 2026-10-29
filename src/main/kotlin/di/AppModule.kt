package di


import datasource.repository.GameRepository
import datasource.repository.GameRepositoryImpl
import datasource.repository.UserRepositoryImpl
import datasource.repository.UserRepository
import domain.service.*
import org.koin.dsl.module

val appModule = module {

    single<UserRepository> { UserRepositoryImpl() }
    single<UserService> { UserServiceImpl(get()) }
    single {
        JwtProvider(
            secretAccess = System.getenv("JWT_ACCESS_SECRET")
                ?: "TEST_SECRET_ACCESS_1234567890123456",
            secretRefresh = System.getenv("JWT_REFRESH_SECRET")
                ?: "TEST_SECRET_REFRESH_1234567890123456"
        )
    }
    single<AuthService> { AuthServiceJwt(get(), get()) }


    single<GameRepository> { GameRepositoryImpl() }
    single<GameService> { GameServiceImpl(get()) }
}
