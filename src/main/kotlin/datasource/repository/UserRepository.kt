package datasource.repository

import domain.model.User
import java.util.UUID

interface UserRepository {
    /** Создаёт нового пользователя. Возвращает true при успехе */
    fun create(user: User): Boolean

    /** Ищет пользователя по логину. Возвращает null, если не найден */
    fun findByLogin(login: String): User?

    /** Ищет пользователя по его UUID. Возвращает null, если не найден */
    fun findById(id: UUID): User?
}
