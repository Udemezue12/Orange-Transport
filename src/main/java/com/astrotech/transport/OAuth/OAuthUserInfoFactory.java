package com.astrotech.transport.OAuth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;

import java.util.Objects;

public final class OAuthUserInfoFactory {

    public static OAuthUserInfo fromOidc(
            OAuthProvider provider,
            OidcUser user
    ) {
        Objects.requireNonNull(user);

        String subject = requireText(
                user.getSubject(),
                "OIDC provider did not return a subject"
        );

        String email = requireText(
                user.getEmail(),
                "OIDC provider did not return an email address"
        );

        String fullName = resolveName(
                user.getFullName(),
                user.getGivenName(),
                email
        );

        return new OAuthUserInfo(
                subject,
                email,
                fullName,
                user.getEmailVerified(),
                provider
        );
    }

    public static OAuthUserInfo fromOAuth2(
            OAuthProvider provider,
            OAuth2User user
    ) {
        Objects.requireNonNull(user);

        return switch (provider) {

            case GITHUB -> fromGithub(user);

            case GOOGLE -> throw new IllegalArgumentException(
                    "Google must be processed through OpenID Connect"
            );
        };
    }

    private static OAuthUserInfo fromGithub(OAuth2User user) {

        Object id = user.getAttribute("id");

        if (id == null) {
            throw authenticationException(
                    "missing_subject",
                    "GitHub account did not return an account ID"
            );
        }

        String subject = String.valueOf(id);

        String email = user.getAttribute("email");

        if (!StringUtils.hasText(email)) {
            throw authenticationException(
                    "missing_email",
                    "GitHub account does not expose an email address"
            );
        }

        String name = user.getAttribute("name");

        if (!StringUtils.hasText(name)) {
            name = user.getAttribute("login");
        }

        if (!StringUtils.hasText(name)) {
            name = email;
        }


        return new OAuthUserInfo(
                subject,
                email,
                name,
                false,
                OAuthProvider.GITHUB
        );
    }

    private static String resolveName(
            String fullName,
            String givenName,
            String fallback
    ) {
        if (StringUtils.hasText(fullName)) {
            return fullName.trim();
        }

        if (StringUtils.hasText(givenName)) {
            return givenName.trim();
        }

        return fallback;
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (!StringUtils.hasText(value)) {
            throw authenticationException(
                    "invalid_user_info",
                    message
            );
        }

        return value.trim();
    }

    private static OAuth2AuthenticationException authenticationException(
            String errorCode,
            String message
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode),
                message
        );
    }
}
