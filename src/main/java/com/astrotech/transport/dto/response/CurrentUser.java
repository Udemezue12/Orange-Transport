package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.UserRole;

import java.util.UUID;

public record CurrentUser(
        UUID userId,
        UserRole role,
        Boolean emailVerified
) {
}
