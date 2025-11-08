package com.example.usersevice.controller

import com.example.usersevice.dto.*
import com.example.usersevice.service.StorageService
import com.example.usersevice.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "User profile management")
@SecurityRequirement(name = "Bearer Authentication")
class UserController(
    private val userService: UserService,
    private val storageService: StorageService
) {

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    fun getCurrentUser(
        authentication: Authentication
    ): ResponseEntity<JSendResponse<UserResponse>> {
        return try {
            val userId = authentication.principal as Long
            val user = userService.getUser(userId)
            ResponseEntity.ok(JSendResponse.success(user))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(JSendResponse.fail(mapOf("error" to "User not found")))
        } as ResponseEntity<JSendResponse<UserResponse>>
    }

    @PostMapping(
        "/avatar",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(
        summary = "Upload user avatar",
        description = "Upload an image file as user avatar. Triggers WebSocket notification."
    )
    fun uploadAvatar(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<JSendResponse<AvatarUploadResponse>> {
        return try {
            val userId = authentication.principal as Long

            // Save file
            val avatarUrl = storageService.saveAvatar(file)

            // Update user
            userService.updateAvatar(userId, avatarUrl)

            ResponseEntity.ok(
                JSendResponse.success(AvatarUploadResponse(avatarUrl))
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(JSendResponse.fail(mapOf("error" to e.message)))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JSendResponse.error("Avatar upload failed"))
        } as ResponseEntity<JSendResponse<AvatarUploadResponse>>
    }

    @DeleteMapping
    @Operation(
        summary = "Delete user account",
        description = "Permanently delete user account, avatar, and disconnect all WebSocket sessions"
    )
    fun deleteUser(
        authentication: Authentication
    ): ResponseEntity<JSendResponse<Map<String, String>>> {
        return try {
            val userId = authentication.principal as Long
            userService.deleteUser(userId)

            ResponseEntity.ok(
                JSendResponse.success(
                    mapOf("message" to "User deleted successfully")
                )
            )
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JSendResponse.error("User deletion failed"))
        } as ResponseEntity<JSendResponse<Map<String, String>>>
    }
}