package domain.model

import java.util.UUID

data class Game(
    val id: UUID,
    val board: Board,
    val mode: GameMode,
    val status: GameStatus,
    val currentPlayer: UUID?,
    val winner: UUID?,
    val playerX: UUID,
    val playerO: UUID?,
    val createdAt: Long
)
