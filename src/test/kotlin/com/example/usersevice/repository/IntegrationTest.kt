package com.example.usersevice.controller

import com.example.usersevice.dto.RegisterRequests
import com.example.usersevice.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun debug() {
        RegisterRequests("ab", "123")
    }

    @Test
    fun `register should create new user and return 201`() {
        val request = RegisterRequests(
            identifier = "tester3",
            password = "tester3!"
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.status") { value("success") }
            jsonPath("$.data.token") { exists() }
            jsonPath("$.data.userId") { exists() }
        }
    }

    @Test
    fun `register should return 400 when user already exists`() {
        val request = RegisterRequests(
            identifier = "tester3",
            password = "tester3!"
        )

        // First registration
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        // Second registration with same identifier
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("fail") }
        }
    }

    @Test
    fun `register should return 400 when validation fails`() {
        val request = RegisterRequests(
            identifier = "ab", // Too short
            password = "123"   // Too short
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("fail") }
        }
    }
}