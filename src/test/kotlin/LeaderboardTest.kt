package test

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlinx.serialization.json.*
import java.util.*

class LeaderboardTest {

    @Test
    fun testLeaderboardFlow() = testApplication {

        println("\n=== ТЕСТ ЛИДЕРБОРДА ===")

        val user1 = UUID.randomUUID()
        val user2 = UUID.randomUUID()

        println("Создаём двух пользователей:")
        println(" - user1 = $user1")
        println(" - user2 = $user2")

        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"u1","password":"123456"}""")
        }

        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"u2","password":"123456"}""")
        }

        println("✔ Пользователи зарегистрированы")

        val login1 = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"u1","password":"123456"}""")
        }.bodyAsText()

        val login2 = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"u2","password":"123456"}""")
        }.bodyAsText()

        val token1 = Json.parseToJsonElement(login1).jsonObject["accessToken"]!!.jsonPrimitive.content
        val token2 = Json.parseToJsonElement(login2).jsonObject["accessToken"]!!.jsonPrimitive.content

        println("✔ Получены токены:")
        println(" - token1 = $token1")
        println(" - token2 = $token2")

        val game1 = client.post("/game/new") {
            header("Authorization", "Bearer $token1")
        }.bodyAsText()

        val gameId1 = Json.parseToJsonElement(game1).jsonObject["id"]!!.jsonPrimitive.content

        println("✔ Создана игра для user1: gameId=$gameId1")

        println("Пытаемся завершить игру победой user1...")

        val boardWin = """
            {"cells":[[1,1,1],[0,0,0],[0,0,0]]}
        """.trimIndent()

        val finishResp = client.post("/game/$gameId1") {
            header("Authorization", "Bearer $token1")
            contentType(ContentType.Application.Json)
            setBody(boardWin)
        }

        println("Ответ сервера на победный ход: ${finishResp.status}")
        println("Тело ответа: ${finishResp.bodyAsText()}")

        val game2 = client.post("/game/new") {
            header("Authorization", "Bearer $token2")
        }.bodyAsText()

        val gameId2 = Json.parseToJsonElement(game2).jsonObject["id"]!!.jsonPrimitive.content

        println("✔ Создана игра для user2: gameId=$gameId2")

        val boardDraw = """
            {"cells":[[1,2,1],[2,1,2],[2,1,2]]}
        """.trimIndent()

        val drawResp = client.post("/game/$gameId2") {
            header("Authorization", "Bearer $token2")
            contentType(ContentType.Application.Json)
            setBody(boardDraw)
        }

        println("Ответ сервера на ничью: ${drawResp.status}")
        println("Тело ответа: ${drawResp.bodyAsText()}")

        println("\nЗапрашиваем лидерборд...")

        val leaderboardResp = client.get("/leaderboard?n=10") {
            header("Authorization", "Bearer $token1")
        }

        println("Статус лидерборда: ${leaderboardResp.status}")
        println("Тело лидерборда: ${leaderboardResp.bodyAsText()}")

        println("\n=== ТЕСТ ЛИДЕРБОРДА ЗАВЕРШЁН ===")
    }
}
