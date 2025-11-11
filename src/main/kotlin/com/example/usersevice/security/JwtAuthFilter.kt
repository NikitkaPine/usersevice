package com.example.usersevice.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.*
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ){

        val path = request.requestURI

        if (path.startsWith("/api/auth/") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/swagger-ui.html") ||
            path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response)
            return
        }
        val authHeader = request.getHeader("Authorization")

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            val token = authHeader.substring(7)
            if(jwtUtil.validateToken(token)){
                val userId =    jwtUtil.getUserIdFromToken(token)

                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    emptyList()
                )

                SecurityContextHolder.getContext().authentication=authentication
            }

        }

        filterChain.doFilter(request,response)
    }
}