package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record RouteRequest(
        @NotNull(message = "Origin Terminal is required")

        UUID originTerminalId,
        @NotNull(message = "Destination Terminal is required")
        UUID destinationTerminalId
) {
}
