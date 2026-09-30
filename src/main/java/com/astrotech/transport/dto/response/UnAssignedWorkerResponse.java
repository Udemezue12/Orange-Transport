package com.astrotech.transport.dto.response;

import java.util.UUID;

public record UnAssignedWorkerResponse(
        UUID userId,
        String fullName,
        String email,
        UUID driverProfileId
) {}
