package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RouteUpdateRequest(

        UUID originTerminalId,
        UUID destinationTerminalId
) {
}
