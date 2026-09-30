package com.astrotech.transport.OAuth;

public record OAuthUserInfo(
        String subject,
        String email,
        String fullName,
        Boolean emailVerified,
        OAuthProvider provider
) {}
