package com.astrotech.transport.dto.response;

public record TripCodeResponse(
        SimpleTripResponse trip,
        VehicleResponse vehicle,
        RouteResponse route,
        DriverProfileResponse driver
) {
}
