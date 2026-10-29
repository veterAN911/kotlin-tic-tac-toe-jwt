package datasource.repository

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import domain.model.GameStatus
import domain.model.GameMode

object GamesTable : UUIDTable("games") {
    val board = text("board")
    val mode = enumerationByName("mode", 15, GameMode::class)
    val status = enumerationByName("status", 15, GameStatus::class)
    val currentPlayer = uuid("current_player").nullable()
    val winner = uuid("winner").nullable()
    val playerX = uuid("player_x")
    val playerO = uuid("player_o").nullable()
    val createdAt = long("created_at").default(0L)
    val lastActivityX = long("last_activity_x").default(0L)
    val lastActivityO = long("last_activity_o").default(0L)

}
