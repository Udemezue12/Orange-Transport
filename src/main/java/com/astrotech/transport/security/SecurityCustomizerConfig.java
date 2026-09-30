package com.astrotech.transport.security;


import com.astrotech.transport.configProperties.AppProperties;
import com.astrotech.transport.csrf.CustomCsrfTokenRepository;
import com.astrotech.transport.csrf.CustomSpaCsrfTokenRequestHandler;
import com.astrotech.transport.webauthn.WebAuthnProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;
import org.springframework.web.cors.CorsConfigurationSource;

import static com.astrotech.transport.webauthn.WebAuthnEndpoints.*;


@Configuration
@RequiredArgsConstructor
public class SecurityCustomizerConfig {
    private final CustomCsrfTokenRepository csrfTokenRepository;
    private final AppProperties appProperties;
    private final CustomSpaCsrfTokenRequestHandler csrfTokenRequestHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final WebAuthnProperties webAuthnProperties;
    private final PublicKeyCredentialCreationOptionsRepository creationOptionsRepository;


    public Customizer<SessionManagementConfigurer<HttpSecurity>> getSessionManagementCustomizer() {
        return s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    public Customizer<CsrfConfigurer<HttpSecurity>> getCsrfConfigurerCustomizer() {

        if (Boolean.TRUE.equals(appProperties.enableCsrf())) {
            return csrf -> csrf
                    .csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(csrfTokenRequestHandler)
                    .ignoringRequestMatchers(
                            "/api/v1/auth/**",
                            "/api/v1/config/auth-providers/**",
                            "/api/v1/cloudinary-uploads/" +
                                    "generate-signed-uploads",
                            "/ws",
                            "/ws/**",
                            "/ws/native",
                            "/ws/native/**",
                            "/actuator/health",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/api/v1/csrf_token",
                            "/",
                            "/login/oauth2/**",
                            "/oauth2/**",
                            "/swagger-ui.html",
                            "/templates/**",
                            PASSKEY_LOGIN_OPTIONS,
                            PASSKEY_LOGIN_VERIFY
                    );
        } else {
            return AbstractHttpConfigurer::disable;
        }
    }

    public Customizer<WebAuthnConfigurer<HttpSecurity>> getWebAuthnConfigurerCustomizer() {
        if (Boolean.TRUE.equals(appProperties.enableWebAuthn())) {
            return webAuthn -> webAuthn
                    .rpId(webAuthnProperties.rpId())
                    .rpName(webAuthnProperties.rpName())
                    .allowedOrigins(webAuthnProperties.webAuthnAllowedOrigins())
                    .creationOptionsRepository(creationOptionsRepository)
                    .disableDefaultRegistrationPage(true);
        } else {
            return AbstractHttpConfigurer::disable;
        }

    }

    public Customizer<HeadersConfigurer<HttpSecurity>> getHeadersConfigurerCustomizer() {
        return headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: https:; " +
                                "connect-src 'self' ws: wss:;"))
                .referrerPolicy(r -> r.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
    }

    public Customizer<CorsConfigurer<HttpSecurity>> getCorsCustomizer() {
        if (Boolean.TRUE.equals(appProperties.enableCors())) {
            return cors -> cors.configurationSource(corsConfigurationSource);
        }
        return AbstractHttpConfigurer::disable;
    }
}

