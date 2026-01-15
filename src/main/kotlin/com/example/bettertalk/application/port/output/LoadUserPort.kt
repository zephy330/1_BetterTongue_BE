package com.example.bettertalk.application.port.output

import com.example.bettertalk.domain.user.User

/**
 * Output Port: 사용자 조회
 */
interface LoadUserPort {

    fun findById(id: Long): User?

    fun findAllByIds(ids: List<Long>): List<User>
}
