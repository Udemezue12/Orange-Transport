package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.AccessTokenResponse;
import com.astrotech.transport.dto.response.RefreshTokenResponse;
import com.astrotech.transport.entities.User;

public class TokenMapper {
    public static RefreshTokenResponse refreshTokenResponse(String accessToken, String refreshToken) {
        return new RefreshTokenResponse(accessToken, refreshToken);
    }
    public static AccessTokenResponse accessTokenResponse(String accessToken, String refreshToken, User user) {
        var tokenResponse = refreshTokenResponse(accessToken, refreshToken);
        var userResponse = UserMapper.response(user);
        return new AccessTokenResponse(tokenResponse, userResponse);
    }
}
