package com.astrotech.transport.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SimpleTerminalRouteResponse(
        UUID id,
        Integer stopOrder,
        Instant stopOrderCreatedAt
) {
}
