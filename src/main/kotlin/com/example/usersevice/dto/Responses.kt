package com.example.usersevice.dto

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: Long? = null,
    val error: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class AvatarUploadResponse(
    val avatarUrl: String
)

data class UserResponse(
    val id: Long,
    val identifier:String,
    val avatarUrl: String?,
    val createdAt: String
)