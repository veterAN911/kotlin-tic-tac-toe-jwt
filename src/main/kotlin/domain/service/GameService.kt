package domain.service

import domain.model.*
import java.util.*

interface GameService {
    /** Создаёт новую игру против бота (игрок X — пользователь) */
    fun createNewGame(playerX: UUID): Game

    /** Создаёт новую игру человек-человек (ожидание второго игрока) */
    fun createNewHumanGame(playerX: UUID): Game

    /** Подключает второго игрока к существующей игре в статусе WAITING */
    fun joinGame(id: UUID, playerO: UUID): Game

    /** Возвращает игру по её UUID или null */
    fun getGame(id: UUID): Game?

    /** Сохраняет текущее состояние игры в репозиторий */
    fun saveGame(game: Game)

    /** Возвращает список игр, ожидающих второго игрока */
    fun getWaitingGames(): List<Game>

    /** Проверяет корректность хода: очерёдность, занятость клетки, статус игры */
    fun validateMove(oldGame: Game, newBoard: Board)

    /** Применяет ход: обновляет доску, переключает игрока, проверяет победу/ничью */
    fun applyMove(game: Game, newBoard: Board): Game

    /** Выполняет ход бота по минимаксному алгоритму */
    fun makeBotMove(game: Game): Game

    /** Обновляет метку активности игрока в игре (защита от таймаута) */
    fun updateActivity(gameId: UUID, playerId: UUID)

    /** Возвращает список завершённых игр для заданного пользователя */
    fun getFinishedGamesHistory(userId: UUID): List<FinishedGameItem>

    /** Возвращает топ-N игроков по винрейту */
    fun getTopPlayers(limit: Int): List<LeaderboardEntry>
}
