package com.example.usersevice.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.usersevice.dto.WebSocket
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class WebSocketService(
    private val objectMapper: ObjectMapper
){
    private val sessions = ConcurrentHashMap<Long,MutableSet<WebSocketSession>>()

    fun registerSession(userId: Long, session: WebSocketSession){
        sessions.computeIfAbsent(userId){
            mutableSetOf()
        }.add(session)

        println("Websocket connected: userId = $userId, sessionId = ${session.id}")
    }

    fun removeSession(userId: Long, session: WebSocketSession){
        sessions[userId]?.remove(session)

        if(sessions[userId]?.isEmpty() == true){
            sessions.remove(userId)
        }

        println("Websocket disconnected: userId = $userId, sessionId = ${session.id}")
    }

    fun notifyAvatarChange(userId: Long, avatarUrl:String){
        val message = WebSocket(
            type = "AVATAR_CHANGED",
            avatarUrl = avatarUrl
        )

        val json = objectMapper.writeValueAsString(message)
        val textMessage = TextMessage(json)

        sessions[userId]?.forEach { session ->
            if (session.isOpen) {
                try {
                    session.sendMessage(textMessage)
                    println("Sent avatar change notification to userId=$userId")
                } catch (e: Exception) {
                    println("Failed to send message: ${e.message}")
                }
            }
        }

    }

    fun disconnectUser(userId: Long){
        sessions[userId]?.forEach{ session ->
            if(session.isOpen){
                try{
                    session.close()
                }catch (e: Exception){
                    println("Failed to close session: ${e.message}")
                }
            }
        }
        sessions.remove(userId)
        println("Disconnected all sessions for userId=$userId")
    }
    fun getActiveSessionCount(userId: Long): Int {
        return sessions[userId]?.size ?: 0
    }

}