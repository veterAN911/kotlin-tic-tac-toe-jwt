package web.module

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import web.route.authJwtRoutes
import web.route.gameRoutes
import web.route.userRoutes
import domain.service.AuthService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val authServiceJwt by inject<AuthService>()

    routing {

        authJwtRoutes(authServiceJwt)

        authenticate("auth-jwt") {
            userRoutes()
            gameRoutes()
        }
    }
}
