package test

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.Test
import web.module.configureFeatures
import web.module.configureRouting

class AuthFlowTest {

    private suspend fun runTest(
        name: String,
        expected: HttpStatusCode,
        request: suspend () -> HttpResponse
    ): Boolean {

        println("\n=== ТЕСТ: $name ===")

        val response = request()
        val body = response.bodyAsText()

        println("Ожидаемый статус: $expected")
        println("Фактический статус: ${response.status}")
        println("Ответ сервера: $body")

        val ok = response.status == expected

        if (ok) println("✔ УСПЕХ: $name")
        else println("✘ ОШИБКА: $name")

        return ok
    }

    @Test
    fun testAuthFlow() = testApplication {

        environment {
            config = MapApplicationConfig().apply {
                put("jwt.secret", "TEST_SECRET")
                put("jwt.issuer", "test")
                put("jwt.audience", "test")
            }
        }

        application {
            configureFeatures()
            configureRouting()

            println("\n=== DEBUG CONFIG ===")
            println(environment.config.toMap())
        }

        val results = mutableListOf<Boolean>()

        results += runTest("Регистрация нового пользователя", HttpStatusCode.OK) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"user19412","password":"123456"}""")
            }
        }

        results += runTest("Повторная регистрация (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"user1","password":"123456"}""")
            }
        }

        results += runTest("Пустой логин (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"","password":"123456"}""")
            }
        }

        results += runTest("Пустой пароль (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"user2","password":""}""")
            }
        }

        results += runTest("Короткий логин (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"u","password":"123456"}""")
            }
        }

        results += runTest("Короткий пароль (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"user3","password":"12"}""")
            }
        }

        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"login":"user1","password":"123456"}""")
        }

        println("\n=== ТЕСТ: Успешный логин ===")
        println("Ожидаемый статус: 200 OK")
        println("Фактический статус: ${loginResponse.status}")
        println("Ответ сервера: ${loginResponse.bodyAsText()}")

        val loginOk = loginResponse.status == HttpStatusCode.OK
        results += loginOk

        var refreshToken = ""
        if (loginOk) {
            val loginJson = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
            refreshToken = loginJson["refreshToken"]!!.jsonPrimitive.content
            println("✔ УСПЕХ: refreshToken = $refreshToken")
        } else {
            println("✘ ОШИБКА: Логин не выполнен")
        }

        results += runTest("Неверный пароль (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"user1","password":"wrong"}""")
            }
        }

        results += runTest("Неизвестный пользователь (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"login":"unknown","password":"123"}""")
            }
        }

        results += runTest("Обновление access токена", HttpStatusCode.OK) {
            client.post("/auth/refresh/access") {
                contentType(ContentType.Application.Json)
                setBody("""{"refreshToken":"$refreshToken"}""")
            }
        }

        results += runTest("Обновление refresh токена", HttpStatusCode.OK) {
            client.post("/auth/refresh/refresh") {
                contentType(ContentType.Application.Json)
                setBody("""{"refreshToken":"$refreshToken"}""")
            }
        }

        results += runTest("Неверный refresh токен (401)", HttpStatusCode.Unauthorized) {
            client.post("/auth/refresh/access") {
                contentType(ContentType.Application.Json)
                setBody("""{"refreshToken":"invalid"}""")
            }
        }

        println("\n====================")
        println("ИТОГОВЫЕ РЕЗУЛЬТАТЫ ТЕСТОВ")
        println("====================")

        results.forEachIndexed { i, ok ->
            println("${i + 1}. ${if (ok) "✔ ПРОШЁЛ" else "✘ НЕ ПРОШЁЛ"}")
        }

        val passed = results.count { it }
        val total = results.size

        println("\nИтог: $passed из $total тестов успешно пройдены")
    }
}
