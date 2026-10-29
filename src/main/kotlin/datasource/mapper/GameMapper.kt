package datasource.mapper

import datasource.model.*
import domain.model.*

fun Game.toDatasource(): DsGame =
    DsGame(
        id = id,
        board = DsBoard(board.cells),
        mode = mode,
        status = status,
        currentPlayer = currentPlayer,
        winner = winner,
        playerX = playerX,
        playerO = playerO,
        createdAt = createdAt
    )

fun DsGame.toDomain(): Game =
    Game(
        id = id,
        board = Board(board.cells),
        mode = mode,
        status = status,
        currentPlayer = currentPlayer,
        winner = winner,
        playerX = playerX,
        playerO = playerO,
        createdAt = createdAt
    )