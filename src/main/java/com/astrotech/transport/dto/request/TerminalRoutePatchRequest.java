package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record TerminalRoutePatchRequest(

        UUID terminalId,

        UUID routeId,
        @Size(min = 1, max = 10, message = "Stop Order cannot be zero oe more than 10")
        Integer stopOrder
) {
}
