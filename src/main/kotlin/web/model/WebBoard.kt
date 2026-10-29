package web.model

import kotlinx.serialization.Serializable

@Serializable
data class WebBoard(
    val cells: List<List<Int>>
)
