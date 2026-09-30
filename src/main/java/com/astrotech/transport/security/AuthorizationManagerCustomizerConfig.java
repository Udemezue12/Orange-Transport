package com.astrotech.transport.security;


import com.astrotech.transport.configProperties.SwaggerProperties;
import com.astrotech.transport.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import static com.astrotech.transport.webauthn.WebAuthnEndpoints.PASSKEY_LOGIN_OPTIONS;
import static com.astrotech.transport.webauthn.WebAuthnEndpoints.PASSKEY_LOGIN_VERIFY;

@Configuration
@RequiredArgsConstructor
public class AuthorizationManagerCustomizerConfig {
    private final SwaggerProperties swaggerProperties;

    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> getAuthorizationManagerRequestMatcherRegistryCustomizer() {
        return auth -> auth
                .requestMatchers("/api/v1/auth/**",
                        "/ws/**",
                        "/ws",
                        "/ws/native",
                        "/ws/native/**",
                        "/api/v1/cloudinary-uploads/generate-signed-uploads",
                        "/api/v1/config/auth-providers/**",
                        "/login/oauth2/**",
                        "/api/v1/admin/**",
                        "/oauth2/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/v1/csrf_token",
                        "/templates/**",
                        "/actuator/health",
                        "/error",
                        PASSKEY_LOGIN_VERIFY,
                        PASSKEY_LOGIN_OPTIONS
                        )
                .permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/",
                        "/index.html",
                        "/**/*.js",
                        "/**/*.css",
                        "/api/v1/cloudinary-uploads/generate-signed-uploads",
                        "/**/*/img",
                        "/assets/**", "/favicon.ico",
                        "/v3/api-docs/**")
                .permitAll()
                .requestMatchers(
                        "/api/v1/admin/**")
                .hasRole(UserRole.ADMIN.name())
                .requestMatchers("/api/v1/cloudinary/**")
                .hasRole(swaggerProperties.role())
                .anyRequest().authenticated();
    }
}
