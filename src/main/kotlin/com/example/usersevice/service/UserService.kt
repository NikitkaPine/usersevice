package com.example.usersevice.service

import com.example.usersevice.dto.AutoResponse
import com.example.usersevice.dto.LoginRequest
import com.example.usersevice.dto.RegisterRequests
import com.example.usersevice.dto.UserResponse
import com.example.usersevice.dto.WebSocket
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
    private val storageService: StorageService
) {

    @Transactional
    fun register(requests: RegisterRequests): AutoResponse{
        if(userRepository.existsByIdentifier(requests.identifier)){
            throw IllegalArgumentException("User with this identifier already exists")
        }
        val user = User(
            identifier = requests.identifier,
            passwordHash = passwordEncoder.encode(requests.password)
        )

        val savedUser = userRepository.save(user)
        val token = jwtUtil.generateToken(savedUser.id!!)

        return AutoResponse(
            token = token,
            userId = savedUser.id!!
        )
    }

    fun login(requests: LoginRequest): AutoResponse{
        val user = userRepository.findByIdentifier(requests.identifier)
            .orElseThrow{ IllegalArgumentException("Invalid credentials") }

        if (!passwordEncoder.matches(requests.password,user.passwordHash)){
            throw IllegalArgumentException("Invalid credentials")
        }

        val token =jwtUtil.generateToken(user.id!!)

        return AutoResponse(
            token = token,
            userId = user.id!!
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

        // Disconnect WebSocket sessions
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