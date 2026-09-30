package com.astrotech.transport.security;

import com.astrotech.transport.OAuth.*;
import com.astrotech.transport.configProperties.SwaggerProperties;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.ratelimit.bucketRatelimit.BucketRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.security.enable-webauthn",
        havingValue = "false",
        matchIfMissing = true
)
public class StandardSecurityConfiguration {
    private static final Logger log =
            LoggerFactory.getLogger(StandardSecurityConfiguration.class);

    private final SwaggerProperties swaggerProperties;
    private final OAuth2SecurityCustomizer oAuth2SecurityCustomizer;
    private final SecurityFilterChainConfig securityFilterChainConfig;
    private final BucketRateLimitFilter rateLimitFilter;
    private final PasswordEncoder passwordEncoder;
    private final SecurityCustomizerConfig securityCustomizerConfig;
    private final UserDetailsService userDetailsService;
    private final AuthorizationManagerCustomizerConfig authorizationManagerCustomizerConfig;

    @Bean
    public AuthenticationProvider authenticationProvider() {

        var provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        log.info("Authentication providers: Standard Method"

        );
        return config.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSwaggerDocsFilterChain(
            HttpSecurity http
    ) throws Exception {

        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(
                swaggerProperties.username(),
                false
        );

        var swaggerUser = User.builder()
                .username(email)
                .password(passwordEncoder.encode(
                        swaggerProperties.password()
                ))
                .roles(swaggerProperties.role())
                .build();

        var swaggerUserDetailsService =
                new InMemoryUserDetailsManager(swaggerUser);

        var swaggerAuthenticationProvider =
                new DaoAuthenticationProvider(
                        swaggerUserDetailsService
                );

        swaggerAuthenticationProvider.setPasswordEncoder(
                passwordEncoder
        );

        var swaggerAuthenticationManager =
                new ProviderManager(
                        swaggerAuthenticationProvider
                );

        return http
                .securityMatcher(
                        "/v3/api-docs/admin",
                        "/v3/api-docs/admin/**"
                )
                .authenticationManager(swaggerAuthenticationManager)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        securityCustomizerConfig
                                .getSessionManagementCustomizer()
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .hasRole(swaggerProperties.role())
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .cors(
                        securityCustomizerConfig
                                .getCorsCustomizer()
                )
                .csrf(
                        securityCustomizerConfig
                                .getCsrfConfigurerCustomizer()
                )
                .sessionManagement(
                        securityCustomizerConfig
                                .getSessionManagementCustomizer()
                )
                .authorizeHttpRequests(
                        authorizationManagerCustomizerConfig
                                .getAuthorizationManagerRequestMatcherRegistryCustomizer()
                )
                .oauth2Login(
                        oAuth2SecurityCustomizer
                                .getOAuth2LoginCustomizer()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        rateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        securityFilterChainConfig,
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                        .accessDeniedHandler(
                                (request, response, e) -> {
                                    response.setStatus(
                                            HttpStatus.FORBIDDEN.value()
                                    );
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.getWriter().write("""
                                            {"success":false,"message":"Access denied"}
                                            """);
                                }
                        )
                )
                .headers(
                        securityCustomizerConfig
                                .getHeadersConfigurerCustomizer()
                )
                .build();
    }
}
