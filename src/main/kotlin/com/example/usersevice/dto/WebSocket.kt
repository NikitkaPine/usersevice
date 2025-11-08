package com.example.usersevice.dto

import java.sql.Timestamp

data class WebSocket(
    val type: String,
    val avatarUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
