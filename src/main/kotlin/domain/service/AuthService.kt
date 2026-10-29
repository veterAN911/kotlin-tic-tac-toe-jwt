package domain.service

import web.model.JwtRequest
import web.model.JwtResponse

interface AuthService {
    /** Регистрация нового пользователя. Возвращает true при успехе */
    fun register(login: String, password: String): Boolean

    /** Авторизация по логину и паролю — возвращает пару токенов (access + refresh) */
    fun authorize(req: JwtRequest): JwtResponse?

    /** Обновление access-токена по действующему refresh-токену */
    fun refreshAccessToken(refreshToken: String): JwtResponse?

    /** Обновление refresh-токена (продление сессии) по действующему refresh-токену */
    fun refreshRefreshToken(refreshToken: String): JwtResponse?
}
