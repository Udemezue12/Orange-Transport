package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.AssignedServiceType;

import java.time.Instant;
import java.util.UUID;

public record SimpleAssignedWorkerResponse(
        UUID id,
        AssignedServiceType assignedServiceType,
        UUID assignedServiceId,
        AssignStatus assignStatus,
        Instant assignedAt
) {
}

