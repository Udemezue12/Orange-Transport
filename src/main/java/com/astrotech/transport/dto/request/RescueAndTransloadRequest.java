package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record RescueAndTransloadRequest(
        @NotNull(message = "Trip Id is required")
        UUID tripId,
        @NotNull(message = "Vehicle Id is required")
        UUID vehicleId,
        @NotNull(message = "Driver Profile Id is required")
        UUID driverProfileId,
        @NotBlank(message = "Incident Description is required")
        @Size(min = 10, max = 1200, message = "Incident Description should not be more than 1200 words")
        String incidentDescription,
        @NotBlank(message = "Incident Reason is required")
        @Size(min = 10, max = 255, message = "Incident Reason should not be more than 255 words")
        String incidentReason,
        @NotBlank(message = "Location is required")
        @Size(min = 3, max = 255, message = "Location should not be more than 255 words")
        String locationName
) {
}
