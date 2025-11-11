package com.example.usersevice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import io.jsonwebtoken.security.SignatureException
import java.util.*

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private var accessExpiration: Long = 120000 //2 minutes
    @Value("\${jwt.refresh-expiration}")
    private var refreshExpiration: Long = 86400000 //24 hours

    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateAccessToken(userId: Long): String{
        val now = Date()
        val expiryDate = Date(now.time + accessExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "access")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "refresh")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token:String): Boolean{
        return try{
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        }catch (e: ExpiredJwtException) {
            println("JWT истёк: ${e.message}")
            false
        } catch (e: MalformedJwtException) {
            println("Неверный формат JWT")
            false
        } catch (e: SignatureException) {
            println("Неверная подпись JWT")
            false
        } catch (e: Exception) {
            println("Ошибка валидации JWT: ${e.message}")
            false
        }
    }

    fun getUserIdFromToken(token: String): Long{
        val claims:Claims =  Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    fun getTokenType(token: String): String? {
        return try {
            val claims: Claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
            claims["type"] as? String
        } catch (e: Exception) {
            null
        }
    }

    fun getAccessTokenExpirationInSeconds(): Long {
        return accessExpiration / 1000
    }

}