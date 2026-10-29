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

class GameBotMassTest {

    @Test
    fun test100GamesBot() = testApplication {

        environment { config = MapApplicationConfig() }

        application {
            configureFeatures()
            configureRouting()
        }

        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"test","password":"123"}""")
        }

        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"test","password":"123"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val tokensJson = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
        val accessToken = tokensJson["accessToken"]!!.jsonPrimitive.content

        fun auth() = "Bearer $accessToken"

        var winsX = 0
        var winsO = 0
        var draws = 0

        repeat(1000) { gameIndex ->

            val createResponse = client.post("/game/new") {
                header(HttpHeaders.Authorization, auth())
            }
            assertEquals(HttpStatusCode.OK, createResponse.status)

            val gameJson = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
            val gameId = gameJson["id"]!!.jsonPrimitive.content

            var status = gameJson["status"]!!.jsonPrimitive.content
            var board = gameJson["board"]!!.jsonObject["cells"]!!.jsonArray

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

                val newBoard = board.mapIndexed { i, row ->
                    row.jsonArray.mapIndexed { j, cell ->
                        if (i == x && j == y) 1 else cell.jsonPrimitive.int
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
                    header(HttpHeaders.Authorization, auth())
                    contentType(ContentType.Application.Json)
                    setBody(moveJson)
                }

                assertEquals(HttpStatusCode.OK, moveResponse.status)

                val after = Json.parseToJsonElement(moveResponse.bodyAsText()).jsonObject
                board = after["board"]!!.jsonObject["cells"]!!.jsonArray
                status = after["status"]!!.jsonPrimitive.content
            }

            val finalJson = Json.parseToJsonElement(
                client.get("/game/$gameId") {
                    header(HttpHeaders.Authorization, auth())
                }.bodyAsText()
            ).jsonObject

            val winner = finalJson["winner"]?.jsonPrimitive?.content

            when (status) {
                "DRAW" -> {
                    draws++
                    println("Game #${gameIndex + 1}: DRAW")
                }

                "FINISHED" -> {
                    if (winner != null) {
                        val px = finalJson["playerX"]!!.jsonPrimitive.content
                        val po = finalJson["playerO"]!!.jsonPrimitive.content

                        if (winner == px) {
                            winsX++
                            println("Game #${gameIndex + 1}: WIN_X (winning combo)")
                        } else {
                            winsO++
                            println("Game #${gameIndex + 1}: WIN_O (winning combo)")
                        }
                    } else {
                        println("Game #${gameIndex + 1}: FINISHED (no winner?)")
                    }
                }

                else -> println("Game #${gameIndex + 1}: unexpected status = $status")
            }

            Thread.sleep(10)
        }

        println("\n=== RESULTS OF 1000 GAMES ===")
        println("X wins: $winsX")
        println("O wins: $winsO")
        println("Draws:  $draws")
    }
}
