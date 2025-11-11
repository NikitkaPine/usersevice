package com.example.usersevice.service

import com.example.usersevice.model.RefreshToken
import com.example.usersevice.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun createRefreshToken(userId: Long, token: String, expiresAt: LocalDateTime): RefreshToken {
        refreshTokenRepository.deleteByUserId(userId)

        val oldTokens = refreshTokenRepository.findByUserId(userId)
        if (oldTokens.isNotEmpty()) {
            refreshTokenRepository.deleteByUserId(userId)
        }

        val refreshToken = RefreshToken(
            token = token,
            userId = userId,
            expiresAt = expiresAt
        )

        return refreshTokenRepository.save(refreshToken)
    }

    fun findByToken(token: String): Optional<RefreshToken> {
        return refreshTokenRepository.findByToken(token)
    }

    fun verifyExpiration(token: RefreshToken): Boolean {
        return if (token.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token)
            false
        } else {
            true
        }
    }

    @Transactional
    fun revokeToken(token: String) {
        refreshTokenRepository.findByToken(token).ifPresent { rt ->
            rt.revoked = true
            refreshTokenRepository.save(rt)
        }
    }

    @Transactional
    fun deleteByUserId(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    fun cleanupExpiredTokens() {
        val count = refreshTokenRepository.findAll()
            .filter { it.expiresAt.isBefore(LocalDateTime.now()) }
            .size

        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now())
    }
}