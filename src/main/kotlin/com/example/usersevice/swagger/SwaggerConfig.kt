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
                    .title("REST API service")
                    .version("0.0.1")
                    .description("""
                # REST API User Management service
                
                ## Features
                - JWT-based stateless authentication
                - OAuth2.0 (access + refresh tokens)
                - User registration and login
                - Avatar upload
                - Real-time WebSocket notifications
                - Complete user deletion
                - JSend response format
                - Docker-ready
                                 
                ## Auth:
                Authorization: Bearer <access-token>
                
                ## How to Use in Swagger
                1. **Register** or **login** via `POST /api/auth/register` or `/api/auth/login`
                2. Copy the `accessToken` from the response
                3. Click the **Authorize** button at the top
                4. Paste the token **without** the `Bearer` prefix
                5. Test all protected endpoints
                                                       
                ## JSend Response Format
                **Success:**
                ```json
                { "status": "success", "data": { ... } }
                ```
                **Fail:**
                ```json
                { "status": "fail", "data": { "field": "error message" } }
                ```
                **Error:**
                ```json
                { "status": "error", "message": "description", "code": 500 }
                ```
                    """.trimIndent())

            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Local development server"),
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