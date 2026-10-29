package domain.service

import web.model.JwtRequest
import web.model.JwtResponse
import java.util.*

class AuthServiceJwt(
    private val userService: UserService,
    private val jwtProvider: JwtProvider
) : AuthService {

    override fun register(login: String, password: String): Boolean {
        val hash = password.hashCode().toString()
        return userService.createUser(login, hash)
    }

    override fun authorize(req: JwtRequest): JwtResponse? {
        val user = userService.getUserByLogin(req.login) ?: return null
        val hash = req.password.hashCode().toString()

        if (user.passwordHash != hash) return null

        return JwtResponse(
            accessToken = jwtProvider.generateAccessToken(user),
            refreshToken = jwtProvider.generateRefreshToken(user)
        )
    }

    override fun refreshAccessToken(refreshToken: String): JwtResponse? {
        if (!jwtProvider.validateRefreshToken(refreshToken)) return null

        val claims = jwtProvider.getClaimsFromRefresh(refreshToken)
        val uuid = UUID.fromString(claims["uuid"].toString())

        val user = userService.getUserById(uuid) ?: return null

        return JwtResponse(
            accessToken = jwtProvider.generateAccessToken(user),
            refreshToken = refreshToken
        )
    }

    override fun refreshRefreshToken(refreshToken: String): JwtResponse? {
        if (!jwtProvider.validateRefreshToken(refreshToken)) return null

        val claims = jwtProvider.getClaimsFromRefresh(refreshToken)
        val uuid = UUID.fromString(claims["uuid"].toString())

        val user = userService.getUserById(uuid) ?: return null

        return JwtResponse(
            accessToken = jwtProvider.generateAccessToken(user),
            refreshToken = jwtProvider.generateRefreshToken(user)
        )
    }
}
