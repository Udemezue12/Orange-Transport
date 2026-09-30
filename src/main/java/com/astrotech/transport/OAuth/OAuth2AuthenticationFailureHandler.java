package com.astrotech.transport.OAuth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.failure-redirect-uri}")
    private String failureRedirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String errorCode = resolveErrorCode(exception);

        String targetUrl =
                UriComponentsBuilder
                        .fromUriString(failureRedirectUri)
                        .queryParam("error", errorCode)
                        .build()
                        .toUriString();

        getRedirectStrategy()
                .sendRedirect(
                        request,
                        response,
                        targetUrl
                );
    }

    private String resolveErrorCode(
            AuthenticationException exception
    ) {

        if (exception instanceof OAuth2AuthenticationException oauthException) {
            String code =
                    oauthException
                            .getError()
                            .getErrorCode();

            if (code != null && !code.isBlank()) {
                return code;
            }
        }

        return "oauth2_authentication_failed";
    }
}
