package test

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.Test

class FinishedGamesTest {

    @Test
    fun testFinishedGamesFlow() = testApplication {

        println("\n=== ТЕСТ ИСТОРИИ ЗАВЕРШЁННЫХ ИГР ===")

        println("Регистрируем пользователя...")

        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"testuser","password":"123456"}""")
        }

        println("✔ Пользователь зарегистрирован")

        println("Логинимся...")

        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"testuser","password":"123456"}""")
        }.bodyAsText()

        println("Ответ логина: $loginResp")

        val token = Json.parseToJsonElement(loginResp)
            .jsonObject["accessToken"]!!.jsonPrimitive.content

        println("✔ Получен токен: $token")

        println("Создаём игру...")

        val newGameResp = client.post("/game/new") {
            header("Authorization", "Bearer $token")
        }.bodyAsText()

        println("Ответ создания игры: $newGameResp")

        val gameId = Json.parseToJsonElement(newGameResp)
            .jsonObject["id"]!!.jsonPrimitive.content

        println("✔ Игра создана: $gameId")

        println("Делаем ход...")

        val moveBody = """
            {"cells":[[1,0,0],[0,0,0],[0,0,0]]}
        """.trimIndent()

        val moveResp = client.post("/game/$gameId") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(moveBody)
        }

        println("Ответ на ход: ${moveResp.status}")
        println("Тело ответа: ${moveResp.bodyAsText()}")

        println("Пытаемся завершить игру...")

        val finishBody = """
            {"cells":[[1,1,1],[0,0,0],[0,0,0]]}
        """.trimIndent()

        val finishResp = client.post("/game/$gameId") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(finishBody)
        }

        println("Ответ завершения игры: ${finishResp.status}")
        println("Тело ответа: ${finishResp.bodyAsText()}")

        println("\nЗапрашиваем историю завершённых игр...")

        val historyResp = client.get("/user/games/finished") {
            header("Authorization", "Bearer $token")
        }

        println("Статус ответа: ${historyResp.status}")

        val body = historyResp.bodyAsText()
        println("Тело ответа: $body")

        println("\n=== ТЕСТ ЗАВЕРШЁН ===")
    }
}
