package com.astrotech.transport.dto.response;

import java.util.UUID;

public record RouteResponse(
        UUID id,
        Double distanceKm,
        Integer estimatedDurationMinutes,
        SimpleTerminalResponse originTerminal,
        SimpleTerminalResponse destinationTerminal



) {
}
