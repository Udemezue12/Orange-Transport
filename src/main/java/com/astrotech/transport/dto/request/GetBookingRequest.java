package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.BookingSessionStatus;
import jakarta.validation.constraints.NotNull;

public record GetBookingRequest(
        @NotNull(message = "Booking status needed")
        BookingSessionStatus status
) {
}
