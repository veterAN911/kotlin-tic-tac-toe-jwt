package datasource.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Logger
import java.sql.DriverManager

object DbPostgres {

    fun init(url: String, user: String, password: String) {

        Logger.info("Инициализация PostgreSQL…")

        try {
            val sysUrl = "jdbc:postgresql://localhost:5432/postgres"
            Logger.info("Подключение к системной базе postgres…")

            DriverManager.getConnection(sysUrl, user, password).use { conn ->
                conn.createStatement().use { stmt ->

                    Logger.info("Проверка существования базы tictactoe…")

                    val rs = stmt.executeQuery(
                        "SELECT 1 FROM pg_database WHERE datname = 'tictactoe'"
                    )

                    if (!rs.next()) {
                        Logger.warn("База tictactoe не найдена. Создаю…")
                        stmt.executeUpdate("CREATE DATABASE tictactoe")
                        Logger.info("База tictactoe успешно создана.")
                    } else {
                        Logger.info("База tictactoe уже существует.")
                    }
                }
            }

            Logger.info("Подключение к базе tictactoe…")

            Database.connect(
                url = url,
                driver = "org.postgresql.Driver",
                user = user,
                password = password
            )

            Logger.info("Создание таблиц (если отсутствуют)…")

            transaction {
                SchemaUtils.create(UsersTable, GamesTable)
            }

            Logger.info("Таблицы успешно созданы или уже существуют.")


            Logger.info("Создание SQL-функции check_game_activity…")

            transaction {
                exec(
                    """
                        CREATE OR REPLACE FUNCTION check_game_activity()
                    RETURNS void AS $$
                    BEGIN
                        -- WAITING > 60 сек
                        UPDATE games
                        SET status = 'INTERRUPTED'
                        WHERE status = 'WAITING'
                          AND mode = 'HUMAN'
                          AND (EXTRACT(EPOCH FROM NOW()) * 1000 - created_at) > 60000;

                        -- IN_PROGRESS: X не активен > 15 сек
                        UPDATE games
                        SET status = 'INTERRUPTED'
                        WHERE status = 'IN_PROGRESS'
                          AND mode = 'HUMAN'
                          AND (EXTRACT(EPOCH FROM NOW()) * 1000 - last_activity_x) > 15000;

                        -- IN_PROGRESS: O не активен > 15 сек
                        UPDATE games
                        SET status = 'INTERRUPTED'
                        WHERE status = 'IN_PROGRESS'
                          AND mode = 'HUMAN'
                          AND (EXTRACT(EPOCH FROM NOW()) * 1000 - last_activity_o) > 15000;
                    END;
                    $$ LANGUAGE plpgsql;
                """.trimIndent()
                )
            }

            Logger.info("Функция check_game_activity создана.")


            Logger.info("Запуск фонового мониторинга активности игр…")

            Thread {
                while (true) {
                    try {
                        transaction {
                            exec("SELECT check_game_activity();")
                        }
                    } catch (e: Exception) {
                        Logger.error("Ошибка check_game_activity: ${e.message}", e)
                    }

                    Thread.sleep(1000)
                }
            }.start()

            Logger.info("Фоновый мониторинг успешно запущен.")

        } catch (e: Exception) {
            Logger.error("Ошибка инициализации БД: ${e.message}", e)
            throw e
        }
    }
}
