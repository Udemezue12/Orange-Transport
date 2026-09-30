package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record PassengerSeatRequest(
        @NotNull(message = "Seat is required")
        UUID seatId,
        @NotBlank(message = "Passenger first name is required")
        @Size(min=2, max = 125, message = "Passenger first name should not exceed 125 characters")
        String passengerFirstName,
        @NotBlank(message = "Passenger last name is required")
        @Size(min = 2, max = 125, message = "Passenger last name should not exceed 125 characters")
        String passengerLastName
) {
}
