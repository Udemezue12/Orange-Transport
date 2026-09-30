package com.astrotech.transport.OAuth;

import java.util.Arrays;

public enum OAuthProvider {

    GOOGLE,
    GITHUB;

    public static OAuthProvider fromRegistrationId(String registrationId) {

        return Arrays.stream(values())
                .filter(provider ->
                        provider.name().equalsIgnoreCase(registrationId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported OAuth provider: "
                                        + registrationId));
    }
}
