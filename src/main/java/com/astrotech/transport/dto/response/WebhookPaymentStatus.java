package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.BookingSessionStatus;

import java.util.UUID;

public record WebhookPaymentStatus(
        UUID bookingId,
        BookingSessionStatus status
) {
}
