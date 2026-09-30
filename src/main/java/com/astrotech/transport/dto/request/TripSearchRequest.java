package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record TripSearchRequest(
        @NotNull UUID originTerminalId,
        @NotNull UUID destinationTerminalId,
        @NotNull
        @FutureOrPresent
        LocalDate travelDate
) {}
