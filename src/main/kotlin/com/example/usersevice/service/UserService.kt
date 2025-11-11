package com.example.usersevice.service

import com.example.usersevice.dto.AuthResponse
import com.example.usersevice.dto.LoginRequest
import com.example.usersevice.dto.RegisterRequests
import com.example.usersevice.dto.UserResponse
import com.example.usersevice.model.User
import com.example.usersevice.repository.UserRepository
import com.example.usersevice.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository : UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val websocketService: WebSocketService,
    private val refreshTokenService: RefreshTokenService,
    private val storageService: StorageService
) {

    @Transactional
    fun register(requests: RegisterRequests): AuthResponse{
        if(userRepository.existsByIdentifier(requests.identifier)){
            throw IllegalArgumentException("User with this identifier already exists")
        }
        val user = User(
            identifier = requests.identifier,
            passwordHash = passwordEncoder.encode(requests.password)
        )

        val savedUser = userRepository.save(user)
        return generateTokenPair(savedUser.id!!)
    }

    fun login(requests: LoginRequest): AuthResponse{
        val user = userRepository.findByIdentifier(requests.identifier)
            .orElseThrow{ IllegalArgumentException("Invalid credentials") }

        if (!passwordEncoder.matches(requests.password,user.passwordHash)){
            throw IllegalArgumentException("Invalid credentials")
        }

        return generateTokenPair(user.id!!)
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthResponse {
        val tokenEntity = refreshTokenService.findByToken(refreshToken)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }

        if (tokenEntity.revoked) {
            throw IllegalArgumentException("Refresh token has been revoked")
        }

        if (!refreshTokenService.verifyExpiration(tokenEntity)) {
            throw IllegalArgumentException("Refresh token expired")
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtUtil.getUserIdFromToken(refreshToken)

        // Проверяем что токен типа refresh
        if (jwtUtil.getTokenType(refreshToken) != "refresh") {
            throw IllegalArgumentException("Token is not a refresh token")
        }

        return generateTokenPair(userId)
    }

    private fun generateTokenPair(userId: Long): AuthResponse {
        val accessToken = jwtUtil.generateAccessToken(userId)
        val refreshToken = jwtUtil.generateRefreshToken(userId)

        refreshTokenService.createRefreshToken(
            userId = userId,
            token = refreshToken,
            expiresAt = LocalDateTime.now().plusDays(30)
        )

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            expiresIn = jwtUtil.getAccessTokenExpirationInSeconds()
        )
    }

    @Transactional
    fun updateAvatar(userId: Long, avatarUrl: String):String{
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("User not found ") }

        user.avatarUrl?.let{storageService.deleteAvatar(it)}

        user.avatarUrl = avatarUrl
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)

        // Notify via WebSocket
        websocketService.notifyAvatarChange(userId, avatarUrl)

        return avatarUrl
    }

    @Transactional
    fun deleteUser(userId:Long){
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("User not found ") }

        user.avatarUrl?.let{ storageService.deleteAvatar(it) }

        userRepository.delete(user)

        refreshTokenService.deleteByUserId(userId)
        websocketService.disconnectUser(userId)

    }

    fun getUser(userId:Long): UserResponse{
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("User not found ") }

        return UserResponse(
            id = user.id!!,
            identifier = user.identifier,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt.toString()
        )
    }
}