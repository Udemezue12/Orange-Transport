package com.astrotech.transport.dto.response;

public record TripResponse(
        SimpleTripResponse trip,
        VehicleResponse vehicle,
        RouteResponse route,
        DriverProfileResponse driver,
        UserResponse createdBy

) {
}

