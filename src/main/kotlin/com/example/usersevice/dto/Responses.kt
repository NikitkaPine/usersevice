package com.example.usersevice.dto

data class AuthResponse(
    val token: String? = null,
    val userId: Long? = null,
    val error: String? = null
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