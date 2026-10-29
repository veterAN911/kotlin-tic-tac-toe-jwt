package web.route

import domain.model.GameMode
import domain.model.GameStatus
import domain.service.UserService
import domain.service.GameService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import web.mapper.toDomain
import web.mapper.toWeb
import web.model.WebBoard
import java.util.*

fun Route.gameRoutes() {

    val gameService by inject<GameService>()
    val userService by inject<UserService>()

    post("/game/new") {
        val principal = call.principal<UserIdPrincipal>()!!
        val userId = UUID.fromString(principal.name)

        val game = gameService.createNewGame(userId)

        gameService.updateActivity(game.id, userId)

        call.respond(game.toWeb())
    }

    post("/game/new/human") {
        val principal = call.principal<UserIdPrincipal>()!!
        val userId = UUID.fromString(principal.name)

        val game = gameService.createNewHumanGame(userId)

        gameService.updateActivity(game.id, userId)

        call.respond(game.toWeb())
    }

    post("/game/{id}/join") {
        val principal = call.principal<UserIdPrincipal>()!!
        val playerO = UUID.fromString(principal.name)

        val id = UUID.fromString(call.parameters["id"])

        val updated = gameService.joinGame(id, playerO)

        gameService.updateActivity(id, playerO)

        call.respond(updated.toWeb())
    }

    post("/game/{id}") {
        try {
            val id = UUID.fromString(call.parameters["id"])
            val game = gameService.getGame(id)
                ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Game not found"))

            if (game.status != GameStatus.IN_PROGRESS) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Игра уже не активна (status=${game.status})")
                )
            }

            val board = call.receive<WebBoard>().toDomain()

            gameService.validateMove(game, board)

            val principal = call.principal<UserIdPrincipal>()!!
            val playerId = UUID.fromString(principal.name)
            gameService.updateActivity(id, playerId)

            var updated = gameService.applyMove(game, board)

            if (updated.mode == GameMode.BOT &&
                updated.status == GameStatus.IN_PROGRESS &&
                updated.currentPlayer == updated.playerO
            ) {
                updated = gameService.makeBotMove(updated)
            }

            gameService.saveGame(updated)
            call.respond(updated.toWeb())

        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }

    get("/game/{id}") {
        val id = UUID.fromString(call.parameters["id"])
        val game = gameService.getGame(id)
            ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Game not found"))

        val principal = call.principal<UserIdPrincipal>()!!
        val userId = UUID.fromString(principal.name)
        gameService.updateActivity(id, userId)

        call.respond(game.toWeb())
    }

    get("/game/{id}/wait") {
        val id = UUID.fromString(call.parameters["id"])
        val start = System.currentTimeMillis()

        val principal = call.principal<UserIdPrincipal>()!!
        val userId = UUID.fromString(principal.name)

        while (System.currentTimeMillis() - start < 3000) {

            val game = gameService.getGame(id)
            if (game == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            gameService.updateActivity(id, userId)

            if (game.status != GameStatus.WAITING) {
                call.respond(game.toWeb())
                return@get
            }

            Thread.sleep(500)
        }

        call.respond(HttpStatusCode.NoContent)
    }

    get("/game/waiting") {
        val waiting = gameService.getWaitingGames()
        call.respond(waiting.map { it.toWeb() })
    }

    get("/leaderboard") {
        @Serializable
        data class LeaderboardItem(
            val userId: String,
            val login: String,
            val ratio: Double
        )

        val nParam = call.request.queryParameters["n"] ?: "10"
        val n = nParam.toIntOrNull() ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Некорректное значение n")
        )

        val top = gameService.getTopPlayers(n)

        val result = top.map { entry ->
            val user = userService.getUserById(entry.userId)
            LeaderboardItem(
                userId = entry.userId.toString(),
                login = user?.login ?: "unknown",
                ratio = entry.winsRatio
            )
        }

        call.respond(result)
    }
}
