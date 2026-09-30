package com.astrotech.transport.dto.response;

public record PasskeyLoginResponse(
        boolean isAuthenticated,
        String accessToken,
        String refreshToken
) {
}
