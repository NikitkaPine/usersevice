package com.example.usersevice.dto

data class AutoResponse(
    val token: String,
    val userId: Long
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