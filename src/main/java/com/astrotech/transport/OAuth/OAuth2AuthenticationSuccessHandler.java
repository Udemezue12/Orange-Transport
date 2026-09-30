package com.astrotech.transport.OAuth;

import com.astrotech.transport.OpenId.CustomOidcUser;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.jwt.JwtTokenIssuanceAndRemoval;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String authorizedRedirectUri;
    private final JwtTokenIssuanceAndRemoval tokenIssuanceAndRemoval;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        User user = extractUser(authentication);

       tokenIssuanceAndRemoval.issueJwtToken(user, response);


        String targetUrl =
                UriComponentsBuilder
                        .fromUriString(authorizedRedirectUri)
                        .build()
                        .toUriString();

        getRedirectStrategy()
                .sendRedirect(
                        request,
                        response,
                        targetUrl
                );
    }

    private User extractUser(
            Authentication authentication
    ) {

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof CustomOidcUser oidcUser) {
            return oidcUser.getUser();
        }

        if (principal instanceof CustomOAuth2User oauth2User) {
            return oauth2User.getUser();
        }

        throw new IllegalStateException(
                "Unsupported OAuth principal type: "
                        + principal.getClass().getName()
        );
    }
}






