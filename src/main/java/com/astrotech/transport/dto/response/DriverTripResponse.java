package com.astrotech.transport.dto.response;

public record DriverTripResponse(
        SimpleTripResponse trip,
        VehicleResponse vehicle,
        RouteResponse route,
        UserResponse createdBy

) {
}
