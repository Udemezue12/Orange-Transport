package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.VehicleClass;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record RouteFareRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,
        @NotNull(message = "Effective from date is required")
        @FutureOrPresent(message = "Effective from date must be in the present or future")
        Instant effectiveFrom,
        @NotNull(message = "Vehicle class is required")
        @NotNull(message = "Effective to date is required")
        Instant effectiveTo,
        VehicleClass vehicleClass

) {
    public RouteFareRequest {
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective To date cannot be before Effective From date");
        }
    }
}
