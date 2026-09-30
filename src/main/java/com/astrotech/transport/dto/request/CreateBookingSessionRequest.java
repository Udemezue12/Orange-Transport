package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.*;

public record CreateBookingSessionRequest (
    @NotNull(message = "Trip ID is required.")
    UUID tripId,

    @NotEmpty(message = "At least one seat must be selected.")
    List<PassengerSeatRequest> passengers



) {
}
