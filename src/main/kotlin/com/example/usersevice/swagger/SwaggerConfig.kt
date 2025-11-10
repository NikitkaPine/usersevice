package com.example.usersevice.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customAPI() : OpenAPI{
        val securitySchemeName = "Bearer Authentication"

        return OpenAPI()
            .info(
                Info()
                    .title("RESTapi service")
                    .version("0.0.1")
                    .description("""
                        ## 🚀 User Management REST API
                        
                        A production-ready RESTful service with the following features:
                        
                        ### ✨ Features
                        - 🔐 **JWT-based stateless authentication**
                        - 👤 **User registration and login**
                        - 🖼️ **Avatar upload and management**
                        - 🔌 **Real-time WebSocket notifications**
                        - 📋 **JSend response format**
                        - 🗑️ **Complete user deletion**
                        
                        ### 🔒 Authentication
                        All protected endpoints require a Bearer token in the Authorization header:
```
                        Authorization: Bearer <your-jwt-token>
```
                        
                        ### 📡 How to Use
                        1. **Register** a new user via `POST /api/auth/register`
                        2. Copy the **token** from the response
                        3. Click the **"Authorize" 🔓** button at the top
                        4. Paste the token (without "Bearer" prefix)
                        5. Now you can test all protected endpoints!
                        
                        ### 🔌 WebSocket Connection
                        To receive real-time avatar change notifications:
```
                        ws://localhost:8080/ws/user?token=<your-jwt-token>
```
                        
                        ### 📨 Response Format (JSend)
                        All responses follow the JSend specification:
                        
                        **Success:**
```json
                        {
                          "status": "success",
                          "data": { ... }
                        }
```
                        
                        **Fail (client error):**
```json
                        {
                          "status": "fail",
                          "data": {
                            "error": "User already exists"
                          }
                        }
```
                        
                        **Error (server error):**
```json
                        {
                          "status": "error",
                          "message": "Internal server error"
                        }
```
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("API Support")
                            .email("support@gmail.com")
                    )
                    .license(
                        License()
                            .name("My own license")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local development server"),
                    Server()
                        .url("https://api.example.com")
                        .description("Production server")
                )
            )
            .addSecurityItem(
                SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                Components()
                    .addSecuritySchemes(securitySchemeName  ,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter your JWT token obtained from /api/auth/register or /api/auth/login")
                    )
            )

    }
}