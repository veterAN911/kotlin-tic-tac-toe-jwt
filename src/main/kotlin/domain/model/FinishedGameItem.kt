package domain.model

import java.util.UUID

data class FinishedGameItem(
    val gameId: UUID,
    val createdAt: Long,
    val opponentLogin: String,
    val result: String
)
