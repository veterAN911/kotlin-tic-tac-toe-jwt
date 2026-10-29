package web.model

import kotlinx.serialization.Serializable

@Serializable
data class JwtResponse(
    val type: String = "Bearer",
    val accessToken: String,
    val refreshToken: String
)
