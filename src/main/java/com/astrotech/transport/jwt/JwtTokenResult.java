package com.astrotech.transport.jwt;

public record JwtTokenResult(
        String accessToken,
        String refreshToken) {
}
