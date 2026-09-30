package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.BookingSessionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBookingSessionStatusRequest(
        @NotNull(message = "Target booking session status is required.")
        BookingSessionStatus status
) {
}
