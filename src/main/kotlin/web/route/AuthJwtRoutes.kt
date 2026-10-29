package web.route

import domain.service.AuthService
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import web.model.JwtRequest
import web.model.RefreshJwtRequest

fun Route.authJwtRoutes(authService: AuthService) {

    post("/auth/login") {
        val req = call.receive<JwtRequest>()
        val tokens = authService.authorize(req)
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        call.respond(tokens)
    }

    post("/auth/refresh/access") {
        val req = call.receive<RefreshJwtRequest>()
        val tokens = authService.refreshAccessToken(req.refreshToken)
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        call.respond(tokens)
    }

    post("/auth/refresh/refresh") {
        val req = call.receive<RefreshJwtRequest>()
        val tokens = authService.refreshRefreshToken(req.refreshToken)
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        call.respond(tokens)
    }

    post("/auth/signup") {
        val req = call.receive<JwtRequest>()

        if (req.login.isBlank() || req.password.isBlank()) {
            return@post call.respond(HttpStatusCode.Unauthorized)
        }

        val ok = authService.register(req.login, req.password)
        if (!ok) {
            return@post call.respond(HttpStatusCode.Unauthorized)
        }

        call.respond(HttpStatusCode.OK)
    }

}
