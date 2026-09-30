package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record TripRequest(

        @NotNull(message = "Scheduled departure time is required")
        @Future(message = "Scheduled departure time must be in the future")
        Instant scheduledDepartureTime,

        @NotNull(message = "Scheduled arrival time is required")
        @Future(message = "Scheduled arrival time must be in the future")
        Instant scheduledArrivalTime,

        @NotNull(message = "Boarding time is required")
        Instant boardingTime,


        @NotNull(message = "Vehicle ID is required")
        UUID vehicleId,

        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotNull(message = "Driver Profile ID is required")
        UUID driverProfileId,
        @NotNull(message = "Vehicle Loader ID is required")
        UUID vehicleLoaderId
) {}


