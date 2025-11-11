package com.example.usersevice.repository

import com.example.usersevice.dto.LoginRequest
import com.example.usersevice.dto.RegisterRequests
import com.example.usersevice.model.User
import com.example.usersevice.security.JwtUtil
import com.example.usersevice.service.RefreshTokenService
import com.example.usersevice.service.StorageService
import com.example.usersevice.service.UserService
import com.example.usersevice.service.WebSocketService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Transactional
class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtUtil: JwtUtil
    private lateinit var webSocketService: WebSocketService
    private lateinit var storageService: StorageService
    private lateinit var userService: UserService
    private lateinit var refreshTokenService: RefreshTokenService

    @BeforeEach
    fun setup(){
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtUtil = mockk()
        webSocketService = mockk()
        storageService = mockk()
        refreshTokenService = mockk(relaxed = true)

        userService = UserService(
            userRepository,
            passwordEncoder,
            jwtUtil,
            webSocketService,
            refreshTokenService,
            storageService
        )
    }

    @Test
    fun `register should create new user and return token`(){
        val request = RegisterRequests(
            identifier = "tester1",
            password = "tester1!"
        )

        val hashedPassword = "hashed_password"
        val savedUser = User(
            id =1L,
            identifier = request.identifier,
            passwordHash = hashedPassword
        )

        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        every{ userRepository.existsByIdentifier(request.identifier)} returns false
        every{passwordEncoder.encode(request.password)}returns hashedPassword
        every { userRepository.save(any()) } returns savedUser
        every { jwtUtil.generateAccessToken(1L) } returns accessToken
        every { jwtUtil.generateRefreshToken(1L) } returns refreshToken
        every { jwtUtil.getAccessTokenExpirationInSeconds() } returns 3600L

        val result = userService.register(request)

        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertEquals(1L, result.userId)
        assertEquals("Bearer", result.tokenType)
        assertEquals(3600L, result.expiresIn)

        verify { userRepository.existsByIdentifier(request.identifier) }
        verify { passwordEncoder.encode(request.password) }
        verify { userRepository.save(any()) }
        verify { jwtUtil.generateAccessToken(1L) }
        verify { jwtUtil.generateRefreshToken(1L) }
        verify { refreshTokenService.createRefreshToken(1L, refreshToken, any()) }
    }
    @Test
    fun `register should throw exception when user already exists`() {
        // Given
        val request = RegisterRequests(
            identifier = "tester1",
            password = "tester1!"
        )

        every { userRepository.existsByIdentifier(request.identifier) } returns true

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.register(request)
        }

        assertEquals("User with this identifier already exists", exception.message)
        verify { userRepository.existsByIdentifier(request.identifier) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `login should return token when credentials are valid`() {
        // Given
        val request = LoginRequest(
            identifier = "tester1",
            password = "tester1!"
        )
        val user = User(
            id = 1L,
            identifier = request.identifier,
            passwordHash = "hashed_password"
        )
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        every { userRepository.findByIdentifier(request.identifier) } returns Optional.of(user)
        every { passwordEncoder.matches(request.password, user.passwordHash) } returns true
        every { jwtUtil.generateAccessToken(1L) } returns accessToken
        every { jwtUtil.generateRefreshToken(1L) } returns refreshToken
        every { jwtUtil.getAccessTokenExpirationInSeconds() } returns 3600L

        // When
        val result = userService.login(request)

        // Then
        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertEquals(1L, result.userId)

        verify { userRepository.findByIdentifier(request.identifier) }
        verify { passwordEncoder.matches(request.password, user.passwordHash) }
    }

    @Test
    fun `login should throw exception when credentials are invalid`() {
        // Given
        val request = LoginRequest(
            identifier = "kukuept",
            password = "wrong_password"
        )
        val user = User(
            id = 1L,
            identifier = request.identifier,
            passwordHash = "hashed_password"
        )

        every { userRepository.findByIdentifier(request.identifier) } returns Optional.of(user)
        every { passwordEncoder.matches(request.password, user.passwordHash) } returns false

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            userService.login(request)
        }

        assertEquals("Invalid credentials", exception.message)
    }

    @Test
    fun `updateAvatar should update user avatar and notify websocket`() {
        // Given
        val userId = 1L
        val _avatarUrl = "/uploads/avatars/new-avatar.jpg"
        val user = User(
            id = userId,
            identifier = "tester1",
            passwordHash = "hashed"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.save(any()) } returns user.apply { avatarUrl = _avatarUrl }
        every { webSocketService.notifyAvatarChange(userId, _avatarUrl) } just Runs
        every { storageService.deleteAvatar(any()) } just Runs
        // When
        val result = userService.updateAvatar(userId, _avatarUrl)

        // Then
        assertEquals(_avatarUrl, result)
        verify { userRepository.save(any()) }
        verify { webSocketService.notifyAvatarChange(userId, _avatarUrl) }
    }

    @Test
    fun `updateAvatar should delete old avatar when exists`() {
        // Given
        val userId = 1L
        val oldAvatarUrl = "/uploads/avatars/old-avatar.jpg"
        val newAvatarUrl = "/uploads/avatars/new-avatar.jpg"
        val user = User(
            id = userId,
            identifier = "tester1",
            passwordHash = "hashed",
            avatarUrl = oldAvatarUrl
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { storageService.deleteAvatar(any()) } just Runs
        every { userRepository.save(any()) } answers { firstArg() }
        every { webSocketService.notifyAvatarChange(userId, newAvatarUrl) } just Runs

        // When
        val result = userService.updateAvatar(userId, newAvatarUrl)

        // Then
        assertEquals(newAvatarUrl, result)
        verify(exactly = 1) { storageService.deleteAvatar(oldAvatarUrl) }
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { webSocketService.notifyAvatarChange(userId, newAvatarUrl) }
    }

    @Test
    fun `deleteUser should delete user and disconnect websocket`() {
        // Given
        val userId = 1L
        val avatarUrl = "/uploads/avatars/avatar.jpg"
        val user = User(
            id = userId,
            identifier = "tester1",
            passwordHash = "hashed",
            avatarUrl = "/uploads/avatars/avatar.jpg"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.delete(user) } just Runs
        every { storageService.deleteAvatar(avatarUrl) } just Runs
        every { webSocketService.disconnectUser(userId) } just Runs

        // When
        userService.deleteUser(userId)

        // Then
        verify { storageService.deleteAvatar(user.avatarUrl!!) }
        verify { userRepository.delete(user) }
        verify { webSocketService.disconnectUser(userId) }
    }
    @Test
    fun `getUser should return user response`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            identifier = "test@example.com",
            passwordHash = "hashed"
        )

        every { userRepository.findById(userId) } returns Optional.of(user)

        // When
        val result = userService.getUser(userId)

        // Then
        assertEquals(userId, result.id)
        assertEquals(user.identifier, result.identifier)
    }

    @Test
    fun `getUser should throw exception when user not found`() {
        // Given
        val userId = 999L

        every { userRepository.findById(userId) } returns Optional.empty()

        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.getUser(userId)
        }
    }

}