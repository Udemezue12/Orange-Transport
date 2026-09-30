package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.BookingSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SimpleBookingSessionWithoutSeatsResponse(
        UUID id,
        String reference,
        BookingSessionStatus status,
        BigDecimal totalAmount,
        Instant expiresAt,
        Instant createdAt
) {
}
