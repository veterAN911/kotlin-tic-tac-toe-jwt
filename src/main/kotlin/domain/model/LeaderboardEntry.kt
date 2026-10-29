package domain.model

import java.util.UUID

data class LeaderboardEntry(
    val userId: UUID,
    val winsRatio: Double
)
