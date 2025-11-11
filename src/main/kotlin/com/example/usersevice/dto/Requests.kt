package com.example.usersevice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.intellij.lang.annotations.Identifier

data class RegisterRequests(
    @field:NotBlank(message = "Identifier is required")
    @field:Size(min = 3,max = 255, message = "Identifier must be between 3 and 255 characters")
    val identifier: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6,max = 100, message = "Password must be between 6 and 100 characters")
    val password: String
)
data class LoginRequest(
    @field:NotBlank(message = "Identifier is required")
    val identifier: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)