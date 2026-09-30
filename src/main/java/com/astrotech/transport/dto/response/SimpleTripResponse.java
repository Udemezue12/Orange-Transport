package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.TripStatus;

import java.time.Instant;
import java.util.UUID;

public record SimpleTripResponse(
        UUID id,
        Instant actualDepartureTime,
        Instant actualArrivalTime,
        TripStatus status,
        String tripCode,
        Instant boardingTime,
        Instant bookingCutoff,
        String delayReason
) {
}
