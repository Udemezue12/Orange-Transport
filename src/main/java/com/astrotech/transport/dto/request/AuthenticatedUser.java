package com.astrotech.transport.dto.request;

import java.security.Principal;

public record AuthenticatedUser(
        String userId,
        String role,
        boolean emailVerified
) implements Principal {

    @Override
    public String getName() {
        return userId;
    }
}
