package datasource.repository

import datasource.mapper.toDomain
import datasource.mapper.toDatasource
import datasource.model.DsBoard
import datasource.model.DsGame
import domain.model.Game
import domain.model.GameStatus
import domain.model.FinishedGameItem
import java.util.UUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import utils.Logger
import java.time.LocalDateTime
import domain.model.LeaderboardEntry


class GameRepositoryImpl : GameRepository {

    private fun encodeBoard(board: List<List<Int>>): String =
        board.flatten().joinToString("") { it.toString() }

    private fun decodeBoard(str: String): List<List<Int>> =
        str.map { it.digitToInt() }.chunked(3)

    override fun save(game: Game) {
        Logger.info("Сохранение игры ${game.id}")

        val ds = game.toDatasource()

        transaction {
            val updated = GamesTable.update({ GamesTable.id eq ds.id }) {
                it[board] = encodeBoard(ds.board.cells)
                it[mode] = ds.mode
                it[status] = ds.status
                it[currentPlayer] = ds.currentPlayer
                it[winner] = ds.winner
                it[playerX] = ds.playerX
                it[playerO] = ds.playerO
                it[createdAt] = game.createdAt
            }

            if (updated == 0) {
                GamesTable.insert {
                    it[id] = ds.id
                    it[board] = encodeBoard(ds.board.cells)
                    it[mode] = ds.mode
                    it[status] = ds.status
                    it[currentPlayer] = ds.currentPlayer
                    it[winner] = ds.winner
                    it[playerX] = ds.playerX
                    it[playerO] = ds.playerO
                    it[createdAt] = game.createdAt
                }
            }
        }
    }


    override fun get(id: UUID): Game? = transaction {
        GamesTable.select { GamesTable.id eq id }
            .map {
                val cells = decodeBoard(it[GamesTable.board])

                DsGame(
                    id = it[GamesTable.id].value,
                    board = DsBoard(cells),
                    mode = it[GamesTable.mode],
                    status = it[GamesTable.status],
                    currentPlayer = it[GamesTable.currentPlayer],
                    winner = it[GamesTable.winner],
                    playerX = it[GamesTable.playerX],
                    playerO = it[GamesTable.playerO],
                    createdAt = it[GamesTable.createdAt]

                ).toDomain()
            }
            .singleOrNull()
    }

    override fun getAll(): List<Game> = transaction {
        GamesTable.selectAll()
            .map {
                val cells = decodeBoard(it[GamesTable.board])

                DsGame(
                    id = it[GamesTable.id].value,
                    board = DsBoard(cells),
                    mode = it[GamesTable.mode],
                    status = it[GamesTable.status],
                    currentPlayer = it[GamesTable.currentPlayer],
                    winner = it[GamesTable.winner],
                    playerX = it[GamesTable.playerX],
                    playerO = it[GamesTable.playerO],
                    createdAt = it[GamesTable.createdAt]
                ).toDomain()
            }
    }

    override fun getWaitingGames(): List<Game> = transaction {
        GamesTable.select { GamesTable.status eq GameStatus.WAITING }
            .map {
                val cells = decodeBoard(it[GamesTable.board])

                DsGame(
                    id = it[GamesTable.id].value,
                    board = DsBoard(cells),
                    mode = it[GamesTable.mode],
                    status = it[GamesTable.status],
                    currentPlayer = it[GamesTable.currentPlayer],
                    winner = it[GamesTable.winner],
                    playerX = it[GamesTable.playerX],
                    playerO = it[GamesTable.playerO],
                    createdAt = it[GamesTable.createdAt]


                ).toDomain()
            }
    }

    override fun updateStatus(id: UUID, status: GameStatus) {
        transaction {
            GamesTable.update({ GamesTable.id eq id }) {
                it[GamesTable.status] = status
            }
        }
    }

    override fun delete(id: UUID) {
        transaction {
            GamesTable.deleteWhere { GamesTable.id eq id }
        }
    }

    override fun updateActivityX(id: UUID, ts: Long) {
        transaction {
            GamesTable.update({ GamesTable.id eq id }) {
                it[lastActivityX] = ts
            }
        }
    }

    override fun updateActivityO(id: UUID, ts: Long) {
        transaction {
            GamesTable.update({ GamesTable.id eq id }) {
                it[lastActivityO] = ts
            }
        }
    }

    override fun getFinishedGamesHistory(userId: UUID): List<FinishedGameItem> = transaction {

        exec(
            """
        SELECT 
            g.id AS game_id,
            g.created_at AS created_at,
            u.login AS opponent_login,
            CASE
                WHEN g.winner = '$userId' THEN 'WIN'
                WHEN g.winner IS NULL THEN 'DRAW'
                ELSE 'LOSS'
            END AS result
        FROM games g
        JOIN users u 
            ON (
                CASE 
                    WHEN g.player_x = '$userId' THEN g.player_o
                    ELSE g.player_x
                END
            ) = u.id
        WHERE 
            (g.status = 'FINISHED' OR g.status = 'DRAW')
            AND ('$userId' = g.player_x OR '$userId' = g.player_o)
        ORDER BY g.created_at DESC;
        """
        ) { rs ->
            val list = mutableListOf<FinishedGameItem>()
            while (rs.next()) {
                list.add(
                    FinishedGameItem(
                        gameId = UUID.fromString(rs.getString("game_id")),
                        createdAt = rs.getLong("created_at"),
                        opponentLogin = rs.getString("opponent_login"),
                        result = rs.getString("result")
                    )
                )
            }
            list
        } ?: emptyList()
    }


    override fun getTopPlayers(limit: Int): List<LeaderboardEntry> = transaction {

        val results = exec(
            """
        SELECT 
            user_id,
            SUM(wins)   AS wins,
            SUM(losses) AS losses,
            SUM(draws)  AS draws
        FROM (
            SELECT 
                player_x AS user_id,
                CASE WHEN status = 'FINISHED' AND winner = player_x THEN 1 ELSE 0 END AS wins,
                CASE WHEN status = 'FINISHED' AND winner != player_x THEN 1 ELSE 0 END AS losses,
                CASE WHEN status = 'DRAW' THEN 1 ELSE 0 END AS draws
            FROM games

            UNION ALL

            SELECT 
                player_o AS user_id,
                CASE WHEN status = 'FINISHED' AND winner = player_o THEN 1 ELSE 0 END AS wins,
                CASE WHEN status = 'FINISHED' AND winner != player_o THEN 1 ELSE 0 END AS losses,
                CASE WHEN status = 'DRAW' THEN 1 ELSE 0 END AS draws
            FROM games
            WHERE player_o IS NOT NULL
        ) AS t
        GROUP BY user_id
        """
        ) { rs ->
            val list = mutableListOf<LeaderboardEntry>()
            while (rs.next()) {
                val userId = UUID.fromString(rs.getString("user_id"))
                val wins = rs.getInt("wins")
                val losses = rs.getInt("losses")
                val draws = rs.getInt("draws")

                val total = wins + losses + draws
                if (total == 0) continue

                val ratio = wins.toDouble() / (losses + draws).coerceAtLeast(1)

                list.add(LeaderboardEntry(userId, ratio))
            }
            list
        } ?: emptyList()

        results
            .sortedByDescending { it.winsRatio }
            .take(limit)
    }

}
