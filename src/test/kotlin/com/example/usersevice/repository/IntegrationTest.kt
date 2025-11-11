    package com.example.usersevice.repository

    import com.example.usersevice.dto.LoginRequest
    import com.example.usersevice.dto.RefreshTokenRequest
    import com.example.usersevice.dto.RegisterRequests
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
    import org.springframework.transaction.annotation.Propagation
    import org.springframework.transaction.annotation.Transactional
    import kotlin.test.assertFalse

    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    @ActiveProfiles("test")
    class IntegrationTest {

        @Autowired
        private lateinit var mockMvc: MockMvc

        @Autowired
        private lateinit var objectMapper: ObjectMapper

        @Autowired
        private lateinit var userRepository: UserRepository

        @Autowired
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @BeforeEach
        fun setup() {
            userRepository.deleteAll()
            refreshTokenRepository.deleteAll()
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
                jsonPath("$.data.accessToken") { exists() }
                jsonPath("$.data.refreshToken") { exists() }
                jsonPath("$.data.userId") { exists() }
                jsonPath("$.data.tokenType") { value("Bearer") }
                jsonPath("$.data.expiresIn") { exists() }
            }
        }

        @Test
        fun `register should return 400 when user already exists`() {
            val request = RegisterRequests(
                identifier = "tester3",
                password = "tester3!"
            )

            mockMvc.post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }

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
                identifier = "ab",
                password = "123"
            )

            mockMvc.post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value("fail") }
            }
        }

        @Test
        fun `login should return tokens when credentials are valid`() {
            // регистрация
            val registerRequest = RegisterRequests(
                identifier = "tester15",
                password = "password12345"
            )

            mockMvc.post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(registerRequest)
            }.andExpect {
                status { isCreated() }
            }

            // Login
            val loginRequest = LoginRequest(
                identifier = "tester15",
                password = "password12345"
            )

            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(loginRequest)
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("success") }
                jsonPath("$.data.accessToken") { exists() }
                jsonPath("$.data.refreshToken") { exists() }
            }
        }
        @Test
        fun `login should return 401 when credentials are invalid`() {
            val request = LoginRequest(
                identifier = "nonexistent@example.com",
                password = "wrongpassword"
            )

            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.status") { value("fail") }
            }
        }

        @Test
        fun `refresh should return new token pair when refresh token is valid`() {
            // Register user
            val registerRequest = RegisterRequests(
                identifier = "test@inbox.lv",
                password = "password12345"
            )

            val registerResponse = mockMvc.post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(registerRequest)
            }.andReturn()

            val registerBody = objectMapper.readTree(registerResponse.response.contentAsString)
            val refreshToken = registerBody["data"]["refreshToken"].asText()
            assertFalse(refreshToken.isBlank())
            // Refresh token
            val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)

            mockMvc.post("/api/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(refreshRequest)
            }.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("success") }
                jsonPath("$.data.accessToken") { exists() }
                jsonPath("$.data.refreshToken") { exists() }
            }
        }

        @Test
        fun `refresh should return 401 when refresh token is invalid`() {
            val request = RefreshTokenRequest(refreshToken = "invalid_token")

            mockMvc.post("/api/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.status") { value("fail") }
            }
        }
    }