package com.astrotech.transport.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken

){

}
