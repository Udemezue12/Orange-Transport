package com.astrotech.transport.dto.response;

public record TripForReservationResponse(
        SimpleTripResponse trip,
        RouteResponse route,
        DriverProfileResponse driver
) {
}
