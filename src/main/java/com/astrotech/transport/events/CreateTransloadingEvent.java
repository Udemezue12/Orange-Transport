package com.astrotech.transport.events;


import java.util.UUID;

public record CreateTransloadingEvent(
        UUID id,
        String incidentDescription,
        String incidentReason,

        String locationName
) {
}
