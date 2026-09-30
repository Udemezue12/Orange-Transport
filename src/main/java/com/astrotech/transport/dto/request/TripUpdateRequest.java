package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.TripStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;
import java.util.UUID;

public record TripUpdateRequest(


        @PastOrPresent(message = "Actual departure time must be in the past or present")
        Instant actualDepartureTime,
        @PastOrPresent(message = "Actual arrival time must be in the past or present")
        Instant actualArrivalTime,

        @Future(message = "Scheduled departure time must be in the future")
        Instant scheduledDepartureTime,
        @Future(message = "Scheduled departure time must be in the future")
        Instant scheduledArrivalTime,
        TripStatus status,
        Instant boardingTime,
        UUID vehicleId,
        UUID routeId,
        UUID driverProfileId,
        UUID vehicleLoaderId

) {
}
