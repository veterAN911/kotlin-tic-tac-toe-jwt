package test

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import web.module.configureFeatures
import web.module.configureRouting

class GameHumanMassTest {

    @Test
    fun test100GamesHumanVsHuman() = testApplication {

        environment { config = MapApplicationConfig() }

        application {
            configureFeatures()
            configureRouting()
        }

        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"p1","password":"123"}""")
        }
        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"p2","password":"123"}""")
        }

        val loginP1 = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"p1","password":"123"}""")
        }
        val loginP2 = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"p2","password":"123"}""")
        }

        assertEquals(HttpStatusCode.OK, loginP1.status)
        assertEquals(HttpStatusCode.OK, loginP2.status)

        val tokenP1 = Json.parseToJsonElement(loginP1.bodyAsText())
            .jsonObject["accessToken"]!!.jsonPrimitive.content

        val tokenP2 = Json.parseToJsonElement(loginP2.bodyAsText())
            .jsonObject["accessToken"]!!.jsonPrimitive.content

        fun authP1() = "Bearer $tokenP1"
        fun authP2() = "Bearer $tokenP2"

        var winsX = 0
        var winsO = 0
        var draws = 0

        repeat(100) { gameIndex ->

            println("\n=== HUMAN GAME #${gameIndex + 1} START ===")

            val createResponse = client.post("/game/new/human") {
                header(HttpHeaders.Authorization, authP1())
            }
            assertEquals(HttpStatusCode.OK, createResponse.status)

            val gameJson = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
            val gameId = gameJson["id"]!!.jsonPrimitive.content

            val joinResponse = client.post("/game/$gameId/join") {
                header(HttpHeaders.Authorization, authP2())
            }
            assertEquals(HttpStatusCode.OK, joinResponse.status)

            var status = "IN_PROGRESS"

            var poll = client.get("/game/$gameId") {
                header(HttpHeaders.Authorization, authP1())
            }
            var game = Json.parseToJsonElement(poll.bodyAsText()).jsonObject

            var board = game["board"]!!.jsonObject["cells"]!!.jsonArray
            var currentPlayer = game["currentPlayer"]!!.jsonPrimitive.content
            val playerX = game["playerX"]!!.jsonPrimitive.content
            val playerO = game["playerO"]!!.jsonPrimitive.content

            while (status == "IN_PROGRESS") {

                val empty = mutableListOf<Pair<Int, Int>>()

                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        if (board[i].jsonArray[j].jsonPrimitive.int == 0) {
                            empty += i to j
                        }
                    }
                }

                if (empty.isEmpty()) break

                val (x, y) = empty.random()
                println("Player $currentPlayer moves to [$x,$y]")

                val newBoard = board.mapIndexed { i, row ->
                    row.jsonArray.mapIndexed { j, cell ->
                        if (i == x && j == y) {
                            if (currentPlayer == playerX) 1 else 2
                        } else cell.jsonPrimitive.int
                    }
                }

                val moveJson = buildJsonObject {
                    put("cells", buildJsonArray {
                        newBoard.forEach { row ->
                            add(buildJsonArray {
                                row.forEach { cell -> add(cell) }
                            })
                        }
                    })
                }.toString()

                val moveResponse = client.post("/game/$gameId") {
                    header(
                        HttpHeaders.Authorization,
                        if (currentPlayer == playerX) authP1() else authP2()
                    )
                    contentType(ContentType.Application.Json)
                    setBody(moveJson)
                }

                assertEquals(HttpStatusCode.OK, moveResponse.status)

                val after = Json.parseToJsonElement(moveResponse.bodyAsText()).jsonObject
                board = after["board"]!!.jsonObject["cells"]!!.jsonArray
                status = after["status"]!!.jsonPrimitive.content
                currentPlayer = after["currentPlayer"]?.jsonPrimitive?.content ?: ""
            }

            println("Human Game #${gameIndex + 1} finished with status: $status")

            val finalPoll = client.get("/game/$gameId") {
                header(HttpHeaders.Authorization, authP1())
            }
            val finalJson = Json.parseToJsonElement(finalPoll.bodyAsText()).jsonObject
            val winner = finalJson["winner"]?.jsonPrimitive?.content

            when {
                status == "DRAW" -> draws++
                winner != null -> {
                    if (winner == playerX) winsX++
                    else winsO++
                }
            }

            Thread.sleep(10)
        }

        println("\n=== RESULTS OF 100 HUMAN VS HUMAN GAMES ===")
        println("X wins: $winsX")
        println("O wins: $winsO")
        println("Draws:  $draws")
    }
}
