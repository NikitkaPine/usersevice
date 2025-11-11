package com.example.usersevice.repository

import com.example.usersevice.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByUserId(userId: Long): List<RefreshToken>
    fun deleteByExpiresAtBefore(date: LocalDateTime)
    fun deleteByUserId(userId: Long)
}