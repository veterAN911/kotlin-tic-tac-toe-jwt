package domain.service

import domain.model.User
import java.util.*

interface UserService {
    /** Создаёт нового пользователя с заданным логином и хешем пароля */
    fun createUser(login: String, passwordHash: String): Boolean

    /** Возвращает пользователя по логину или null */
    fun getUserByLogin(login: String): User?

    /** Возвращает пользователя по UUID или null */
    fun getUserById(id: UUID): User?
}
