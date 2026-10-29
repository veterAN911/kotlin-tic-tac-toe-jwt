package main

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    routing {
        staticResources("/", "static")
    }
}
