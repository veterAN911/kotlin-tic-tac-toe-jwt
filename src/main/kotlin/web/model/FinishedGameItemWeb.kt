package web.model

import kotlinx.serialization.Serializable

@Serializable
data class FinishedGameItemWeb(
    val gameId: String,
    val createdAt: Long,
    val opponentLogin: String,
    val result: String
)
