package com.example.usersevice.repository

import com.example.usersevice.dto.LoginRequest
import com.example.usersevice.dto.RegisterRequests
import com.example.usersevice.model.User
import com.example.usersevice.security.JwtUtil
import com.example.usersevice.service.StorageService
import com.example.usersevice.service.UserService
import com.example.usersevice.service.WebSocketService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtUtil: JwtUtil
    private lateinit var webSocketService: WebSocketService
    private lateinit var storageService: StorageService
    private lateinit var userService: UserService

    @BeforeEach
    fun setup(){
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtUtil = mockk()
        webSocketService = mockk()
        storageService = mockk()

        userService = UserService(
            userRepository,
            passwordEncoder,
            jwtUtil,
            webSocketService,
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

        val token = "jwt_token"

        every{ userRepository.existsByIdentifier(request.identifier)} returns false
        every{passwordEncoder.encode(request.password)}returns hashedPassword
        every{userRepository.save(any())}returns savedUser
        every{jwtUtil.generateAccessToken(1L)} returns token

        val result = userService.register(request)

        assertEquals(token, result.accessToken)
        assertEquals(1L, result.userId)

        verify { userRepository.existsByIdentifier(request.identifier) }
        verify { passwordEncoder.encode(request.password) }
        verify { userRepository.save(any()) }
        verify { jwtUtil.generateAccessToken(1L) }
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
        val token = "jwt_token"

        every { userRepository.findByIdentifier(request.identifier) } returns Optional.of(user)
        every { passwordEncoder.matches(request.password, user.passwordHash) } returns true
        every { jwtUtil.generateAccessToken(1L) } returns token

        // When
        val result = userService.login(request)

        // Then
        assertEquals(token, result.accessToken)
        assertEquals(1L, result.userId)
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
        verify { webSocketService.notifyAvatarChange(userId, _avatarUrl) }
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

}