package utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {

    private val logFile = File("game-log.txt")
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @Synchronized
    private fun write(level: String, message: String) {
        val time = LocalDateTime.now().format(formatter)

        val stack = Thread.currentThread().stackTrace
        val element = stack.firstOrNull {
            it.className !in listOf(
                Logger::class.java.name,
                "java.lang.Thread"
            )
        }

        val location = if (element != null) {
            "${element.className}.${element.methodName}"
        } else {
            "UnknownLocation"
        }

        val line = "[$time] [$level] [$location] $message"
        logFile.appendText(line + "\n")
    }

    fun info(msg: String) = write("INFO", msg)
    fun warn(msg: String) = write("WARN", msg)
    fun error(msg: String, e: Exception) = write("ERROR", msg)
}
