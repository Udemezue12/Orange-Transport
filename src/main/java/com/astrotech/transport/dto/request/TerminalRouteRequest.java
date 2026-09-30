package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record TerminalRouteRequest(
        @NotNull(message = "Terminal Id is required")
        UUID terminalId,
        @NotNull(message = "Route Id is required")
        UUID routeId,
        @Size(min = 1, max = 10, message = "Stop Order cannot be zero oe more than 10")
        Integer stopOrder
) {
}
