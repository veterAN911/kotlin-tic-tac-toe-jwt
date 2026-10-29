package web.model

import kotlinx.serialization.Serializable

@Serializable
data class WebGame(
    val id: String,
    val board: WebBoard,
    val mode: String,
    val status: String,
    val currentPlayer: String?,
    val winner: String?,
    val playerX: String,
    val playerO: String?
)


