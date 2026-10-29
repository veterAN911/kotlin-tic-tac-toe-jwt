package web.route

import domain.service.UserService
import domain.service.GameService
import web.mapper.toWeb
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*
import web.model.FinishedGameItemWeb

fun Route.userRoutes() {

    val userService by inject<UserService>()
    val gameService by inject<GameService>()

    get("/user/{id}") {
        val idParam = call.parameters["id"]
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing user id")
            )

        val userId = try {
            UUID.fromString(idParam)
        } catch (e: Exception) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid UUID")
            )
        }

        val user = userService.getUserById(userId)
            ?: return@get call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "User not found")
            )

        call.respond(
            mapOf(
                "id" to user.id.toString(),
                "login" to user.login
            )
        )
    }

    get("/user/games/finished") {
        val principal = call.principal<UserIdPrincipal>()!!
        val userId = UUID.fromString(principal.name)

        val history = gameService.getFinishedGamesHistory(userId)

        val response = history.map { item ->
            FinishedGameItemWeb(
                gameId = item.gameId.toString(),
                createdAt = item.createdAt,
                opponentLogin = item.opponentLogin,
                result = item.result
            )
        }

        call.respond(response)
    }

}
