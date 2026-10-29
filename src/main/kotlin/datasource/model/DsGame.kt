package datasource.model

import domain.model.GameMode
import domain.model.GameStatus
import java.util.UUID

data class DsGame(
    val id: UUID,
    val board: DsBoard,
    val mode: GameMode,
    val status: GameStatus,
    val currentPlayer: UUID?,
    val winner: UUID?,
    val playerX: UUID,
    val playerO: UUID?,
    val createdAt: Long
)
