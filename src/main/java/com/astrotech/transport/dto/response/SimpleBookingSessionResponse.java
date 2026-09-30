package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.BookingSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SimpleBookingSessionResponse(
        UUID id,
        String reference,
        BookingSessionStatus status,
        BigDecimal totalAmount,
        Instant expiresAt,
        Instant createdAt,
        List<UUID> seatIds

) {
}
