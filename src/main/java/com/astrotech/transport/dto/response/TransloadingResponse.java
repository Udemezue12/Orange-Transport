package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.TransloadingStatus;

import java.time.Instant;
import java.util.UUID;

public record TransloadingResponse(
        UUID id,
        String incidentReason,
        String incidentDescription,
        TransloadingStatus status,
        String locationName,
        Instant reportedAt,
        Instant resolvedAt
) {
}
