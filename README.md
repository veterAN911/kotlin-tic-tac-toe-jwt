# Крестики-нолики — backend с JWT-авторизацией

Учебный проект (21 School, AP1_Kt_T05B). Сервер на Ktor: игра в крестики-нолики,
регистрация и вход по логину/паролю, access/refresh JWT, хранение пользователей и
игр в PostgreSQL. В комплекте — статический веб-интерфейс.

## Стек

- Kotlin 1.9, JDK 21, Gradle
- Ktor 2.3 (Netty), Koin (DI)
- PostgreSQL + Exposed (ORM/DSL)
- JWT: `jjwt` + `ktor-server-auth-jwt`
- Фронтенд: HTML/JS в `src/main/resources/static`

## Запуск

Нужен PostgreSQL на `localhost:5432`. При старте приложение само создаёт базу
`tictactoe` и таблицы, если их нет.

Параметры подключения и секреты JWT задаются переменными окружения:

| Переменная | По умолчанию |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/tictactoe` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `JWT_ACCESS_SECRET` | тестовое значение из кода |
| `JWT_REFRESH_SECRET` | тестовое значение из кода |

```bash
export DB_PASSWORD=ваш_пароль
./gradlew run
```

Сервер поднимается на <http://localhost:8099>, там же доступен веб-интерфейс.

## API

Авторизация (без токена):

- `POST /auth/signup` — регистрация
- `POST /auth/login` — вход, выдаёт access + refresh токены
- `POST /auth/refresh/access` — новый access по refresh
- `POST /auth/refresh/refresh` — новый refresh

Игры (требуют `Authorization: Bearer <access>`):

- `POST /game/new` — новая игра против бота
- `POST /game/new/human` — новая игра против человека
- `POST /game/{id}/join` — присоединиться к игре
- `POST /game/{id}` — сделать ход
- `GET /game/{id}` — состояние игры
- `GET /game/{id}/wait` — ожидание соперника
- `GET /game/waiting` — список игр, ждущих игрока
- `GET /leaderboard` — таблица лидеров
- `GET /user/{id}`, `GET /user/games/finished` — профиль и завершённые игры

## Структура

```
src/main/kotlin/
  domain/        модель и бизнес-логика (Game, Board, BotAlgorithmGame, JwtProvider)
  datasource/    PostgreSQL: таблицы, репозитории, мапперы
  web/           Ktor-роуты, модули приложения, DTO
  di/            Koin-модули
```

## Тесты

```bash
./gradlew test
```

Есть тесты на 100 партий с ботом и человеком, flow авторизации, завершённые игры
и таблицу лидеров.
