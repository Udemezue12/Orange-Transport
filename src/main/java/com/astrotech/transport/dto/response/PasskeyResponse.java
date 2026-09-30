package com.astrotech.transport.dto.response;

import java.time.Instant;

public record PasskeyResponse(
        String id,
        String label,
        Instant createdAt,
        Instant lastUsedAt
) {
}
