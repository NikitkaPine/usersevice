package com.example.usersevice.websocket

import com.example.usersevice.websocket.UserWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val userWebSocketHandler: UserWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(userWebSocketHandler, "/ws/user")
            .setAllowedOrigins("*") // В продакшене указать конкретные домены
    }
}