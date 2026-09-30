package com.astrotech.transport.config;


import com.astrotech.transport.configProperties.NotificationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    private final NotificationProperties notificationProperties;
    private static final String BEARER_AUTH = "BearerAuth";
    private static final String CSRF_AUTH = "CsrfAuth";
    private static final String IDEMPOTENCY_AUTH = "IdempotencyAuth";
    @Value("${app.application.author}")
    private String name;
    @Value("${app.application.author-email}")
    private String email;
    @Value("${app.application.author-url}")
    private String url;


    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .info(apiInfo())

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(BEARER_AUTH)
                                .addList(CSRF_AUTH)
                                .addList(IDEMPOTENCY_AUTH)
                )

                .components(
                        new Components()

                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        bearerSecurityScheme()
                                )

                                .addSecuritySchemes(
                                        CSRF_AUTH,
                                        csrfSecurityScheme()
                                )
                                .addSecuritySchemes(
                                        IDEMPOTENCY_AUTH,
                                        idempotencySecurityScheme()
                                )

                );
    }

    private Info apiInfo() {

        return new Info()
                .title(notificationProperties.uniqueName() + " Platform")
                .version("v1.0.0")
                .description("""
                        Backend REST API powering the ORANGE Transport Platform.
                        
                        The API provides secure and scalable services for managing users,
                        drivers, routes, terminals, trips, reservations, tickets, seats,
                        payments, and media uploads.
                        
                        Features:
                        - Authentication & Authorization
                        - User & Profile Management
                        - Driver Profile Management
                        - Cloudinary Media Uploads
                        - Route & Fare Management
                        - Terminal & Terminal Route Management
                        - Vehicle & Seat Management
                        - Trip Management
                        - Trip Reservations & Booking
                        - Ticket Management
                        - Payment Processing
                        
                        
                        All protected endpoints require:
                        1. JWT Access Token
                        2. CSRF Token (for state-changing requests)
                        """)
                .contact(
                        new Contact()
                                .name(name)
                                .email(email)
                                .url(url)
                )
                .license(
                        new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                );
    }

    private SecurityScheme bearerSecurityScheme() {

        return new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        JWT Authorization Header
                        
                        Example:
                        
                        Bearer eyJhbGciOiJIUzI1NiIs...
                        """);
    }

    private SecurityScheme csrfSecurityScheme() {

        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-XSRF-TOKEN")
                .description("""
                        CSRF Token Header
                        
                        Required for:
                        - POST
                        - PUT
                        - PATCH
                        - DELETE
                        
                        Example:
                        
                        X-XSRF-TOKEN: abc123xyz
                        """);
    }

    private SecurityScheme idempotencySecurityScheme() {

        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Idempotency-Key")
                .description("""
                        Unique key used to prevent duplicate requests.
                        
                        Recommended for:
                        - Payments Processing
                        
                        Example:
                        
                        Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
                        
                        A repeated request with the same key
                        will return the original response instead
                        of creating a duplicate resource.
                        """);
    }


}