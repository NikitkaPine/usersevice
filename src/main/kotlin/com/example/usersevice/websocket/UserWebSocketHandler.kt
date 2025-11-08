package com.example.usersevice.websocket

import com.example.usersevice.security.JwtUtil
import com.example.usersevice.service.WebSocketService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class UserWebSocketHandler(
    private val jwtUtil: JwtUtil,
    private val websocketService: WebSocketService
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession){
        val token = extractToken(session)

        if (token == null){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token missing"))
            return
        }
        if (!jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"))
            return
        }

        val userId = jwtUtil.getUserIdFromToken(token)
        websocketService.registerSession(userId,session)

        val welcomeMsg = """{"type":"CONNECTED","message":"WebSocket connected successfully"}"""
        session.sendMessage(TextMessage(welcomeMsg))
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ){
        val token = extractToken(session)
        if(token !=null && jwtUtil.validateToken(token)){
            val userId = jwtUtil.getUserIdFromToken(token)
            websocketService.removeSession(userId,session)
        }
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage
    ) {
        // Echo back for ping/pong
        session.sendMessage(message)
    }

    private fun extractToken(session: WebSocketSession): String?{
        val query = session.uri?.query ?: return null
        return query.split("&")
            .find{it.startsWith("token=")}
            ?.substringAfter("token=")
    }
}