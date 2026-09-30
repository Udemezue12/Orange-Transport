package com.astrotech.transport.dto.response;


import com.astrotech.transport.enums.*;

import java.util.UUID;

public record UserResponse(
        UUID Id,
        String fullName,
        String email,
        UserStatus status,
        String phoneNumber,
        UserRole role,
        Boolean verified
) {
}
