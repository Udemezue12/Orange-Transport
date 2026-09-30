package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record SimpleTripSeatReservationResponse(
        UUID id,
        String passengerName,
        ReservationStatus status,
        Instant expiresAt,
        Instant reservedAt,
        Instant checkedInAt,
        Long version
) {
}
