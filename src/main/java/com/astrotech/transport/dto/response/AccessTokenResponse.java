package com.astrotech.transport.dto.response;

public record AccessTokenResponse(
        RefreshTokenResponse tokenResponse,
        UserResponse userResponse

) {

}
