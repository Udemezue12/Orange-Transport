package com.astrotech.transport.security;

import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.jwt.ExtractJwtToken;
import com.astrotech.transport.jwt.JwtAuthenticationFactory;
import com.astrotech.transport.jwt.JwtProvider;

import com.astrotech.transport.service.BlacklistedTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilterChainConfig extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final BlacklistedTokenService blacklistedTokenService;
    private final ExtractJwtToken extractJwtToken;
    private final JwtAuthenticationFactory jwtAuthenticationFactory;



    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = extractJwtToken.getAccessTokenForJwt(request);


            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {


                var jwt = jwtProvider.extractClaims(token, JwtType.ACCESS);

                if (jwt.isToken()) {
                    var jti = jwt.id();
                    if (!blacklistedTokenService.isBlacklisted(jti)) {
                        var role = String.valueOf(jwt.role());

                        var emailVerified = jwt.emailVerified();
                        var userId = jwt.userId();

                        var auth = jwtAuthenticationFactory.create(userId, UserRole.valueOf(role),
                                emailVerified, true, request);
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        MDC.put("userId", userId);
                    }
                }
            }
        } catch (Exception e) {

            SecurityContextHolder.clearContext();
            log.warn("JWT authentication failed: {}", e.getMessage());
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove("userId");
            }
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/actuator/health")
                || path.equals("/")
                || path.equals("/api/v1/admin/")
                || path.startsWith("/api/v1/cloudinary-uploads/generate-signed-uploads")
                || path.equals("/index.html")
                || path.equals("/api/v1/config/auth-providers/**")
                || path.endsWith(".js")
                || path.endsWith(".css")
                || path.startsWith("/assets/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/v3/api-docs/admin")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/login/oauth2/**")
                || path.startsWith("/oauth2/**")
                || path.equals("/favicon.ico");
    }


}
