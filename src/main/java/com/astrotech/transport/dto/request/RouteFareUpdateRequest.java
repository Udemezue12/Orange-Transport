package com.astrotech.transport.dto.request;
import com.astrotech.transport.enums.VehicleClass;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;




public record RouteFareUpdateRequest(
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @FutureOrPresent(message = "Effective from date must be in the present or future")
        Instant effectiveFrom,

        VehicleClass vehicleClass,

        Instant effectiveTo,
        Boolean active

) {
    public RouteFareUpdateRequest {
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo date cannot be before effectiveFrom date");
        }
    }
}