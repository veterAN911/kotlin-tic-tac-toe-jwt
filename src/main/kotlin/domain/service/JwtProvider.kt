package domain.service

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import java.util.*
import domain.model.User

class JwtProvider(
    secretAccess: String,
    secretRefresh: String
) {

    private val accessKey = Keys.hmacShaKeyFor(secretAccess.toByteArray())
    private val refreshKey = Keys.hmacShaKeyFor(secretRefresh.toByteArray())

    private val accessExpiration = 1000 * 60 * 15
    private val refreshExpiration = 1000 * 60 * 60 * 24

    fun generateAccessToken(user: User): String =
        Jwts.builder()
            .setSubject("access")
            .setExpiration(Date(System.currentTimeMillis() + accessExpiration))
            .addClaims(mapOf("uuid" to user.id.toString()))
            .signWith(accessKey)
            .compact()

    fun generateRefreshToken(user: User): String =
        Jwts.builder()
            .setSubject("refresh")
            .setExpiration(Date(System.currentTimeMillis() + refreshExpiration))
            .addClaims(mapOf("uuid" to user.id.toString()))
            .signWith(refreshKey)
            .compact()

    fun validateAccessToken(token: String): Boolean =
        try {
            Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }

    fun validateRefreshToken(token: String): Boolean =
        try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }

    fun getClaimsFromAccess(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(accessKey)
            .build()
            .parseClaimsJws(token)
            .body

    fun getClaimsFromRefresh(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(refreshKey)
            .build()
            .parseClaimsJws(token)
            .body
}
