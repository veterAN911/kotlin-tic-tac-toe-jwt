package domain.service

import domain.model.User
import datasource.repository.UserRepository
import java.util.*

class UserServiceImpl(
    private val repo: UserRepository
) : UserService {

    override fun createUser(login: String, passwordHash: String): Boolean {
        if (repo.findByLogin(login) != null) return false
        return repo.create(User(login = login, passwordHash = passwordHash))
    }

    override fun getUserByLogin(login: String): User? =
        repo.findByLogin(login)

    override fun getUserById(id: UUID): User? =
        repo.findById(id)
}
