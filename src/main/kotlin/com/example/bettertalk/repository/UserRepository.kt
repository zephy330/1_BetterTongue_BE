package com.example.bettertalk.repository

import com.example.bettertalk.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
