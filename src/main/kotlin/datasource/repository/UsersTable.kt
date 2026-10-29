package datasource.repository

import org.jetbrains.exposed.dao.id.UUIDTable

object UsersTable : UUIDTable("users") {
    val login = varchar("login", 64).uniqueIndex()
    val passwordHash = text("password_hash")
}
