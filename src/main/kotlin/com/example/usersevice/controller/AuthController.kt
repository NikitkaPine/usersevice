package com.example.usersevice.controller

import com.example.usersevice.dto.*
import com.example.usersevice.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login")
class AuthController(
    private val userService: UserService
) {
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with identifier and password"
    )
    fun register(
        @Valid @RequestBody request: RegisterRequests
    ): ResponseEntity<JSendResponse<AutoResponse>> {
        return try {
            val response = userService.register(request)
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JSendResponse.success(response))
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(JSendResponse.success(AutoResponse(error = e.message)))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JSendResponse.success(AutoResponse(error = "Registration failed")))
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login user",
        description = "Authenticate user and return JWT token"
    )
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<JSendResponse<AutoResponse>>{
        return try {
            val response = userService.login(request)
            ResponseEntity.ok(JSendResponse.success(response))
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(JSendResponse.success(AutoResponse(error = "Registration failed")))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JSendResponse.success(AutoResponse(error = "Registration failed")))
        }
    }



}