package domain.model

import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    val login: String,
    val passwordHash: String
)
