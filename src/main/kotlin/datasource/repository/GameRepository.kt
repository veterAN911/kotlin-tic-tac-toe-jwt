package datasource.repository

import domain.model.Game
import domain.model.GameStatus
import domain.model.FinishedGameItem
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import domain.model.LeaderboardEntry

interface GameRepository {
    /** Сохраняет или обновляет игру в базе данных */
    fun save(game: Game)

    /** Возвращает игру по её UUID или null, если не найдена */
    fun get(id: UUID): Game?

    /** Возвращает список всех игр (для отладки и списка доступных) */
    fun getAll(): List<Game>

    /** Возвращает список игр, ожидающих второго игрока (статус WAITING) */
    fun getWaitingGames(): List<Game>

    /** Обновляет статус игры (например, после совершения хода) */
    fun updateStatus(id: UUID, status: GameStatus)

    /** Удаляет игру из базы данных по UUID */
    fun delete(id: UUID)

    /** Обновляет метку времени последней активности игрока X */
    fun updateActivityX(id: UUID, ts: Long)

    /** Обновляет метку времени последней активности игрока O */
    fun updateActivityO(id: UUID, ts: Long)

    /** Возвращает историю завершённых игр для заданного пользователя */
    fun getFinishedGamesHistory(userId: UUID): List<FinishedGameItem>

    /** Возвращает топ-N игроков по рейтингу (W/(W+L+D)) */
    fun getTopPlayers(limit: Int): List<LeaderboardEntry>
}
