package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.UserStatus;

import java.util.UUID;

public record ProfileResponse(
        SimpleProfileResponse profileResponse,
        SimpleIdentityDocumentResponse documentResponse,
        UserResponse userResponse
) {
}
