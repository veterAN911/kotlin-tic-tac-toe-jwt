package web.module

import di.appModule
import domain.service.AuthService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import java.util.*
import domain.service.JwtProvider

fun Application.configureFeatures() {

    datasource.repository.DbPostgres.init(
        url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/tictactoe",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "postgres"
    )

    install(Koin) {
        modules(appModule)
    }

    install(ContentNegotiation) {
        json()
    }

    configureAuth()
}

fun Application.configureAuth() {
    val jwtProvider by inject<JwtProvider>()

    install(Authentication) {
        bearer("auth-jwt") {
            authenticate { tokenCredential ->

                val token = tokenCredential.token

                if (!jwtProvider.validateAccessToken(token)) {
                    return@authenticate null
                }

                val claims = jwtProvider.getClaimsFromAccess(token)
                val uuid = claims["uuid"]?.toString() ?: return@authenticate null

                UserIdPrincipal(uuid)
            }
        }
    }
}
