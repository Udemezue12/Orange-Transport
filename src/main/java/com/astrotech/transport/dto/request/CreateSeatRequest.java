package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;


public record CreateSeatRequest(



        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,

        @NotEmpty(message = "Seats per row configuration cannot be empty or null")
        List<@NotNull(message = "Seat count per row cannot be null")
        @Positive(message = "Seat count per row must be at least 1") Integer> seatsPerRow

) {}
