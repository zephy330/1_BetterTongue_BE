package com.example.bettertalk.adapter.persistence

import com.example.bettertalk.application.port.output.LoadUserPort
import com.example.bettertalk.domain.user.User
import com.example.bettertalk.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val userRepository: UserRepository
) : LoadUserPort {

    override fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    override fun findAllByIds(ids: List<Long>): List<User> {
        return userRepository.findAllById(ids)
    }
}
