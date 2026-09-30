package com.astrotech.transport.webauthn;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WebAuthnFailureHandler
        implements AuthenticationFailureHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WebAuthnFailureHandler.class
            );

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        log.warn(
                "WebAuthn authentication failed: {}",
                exception.getMessage()
        );

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write("""
                {
                    "authenticated": false,
                    "message": "Passkey authentication failed"
                }
                """);
    }
}
