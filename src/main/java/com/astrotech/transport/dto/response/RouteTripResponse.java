package com.astrotech.transport.dto.response;

public record RouteTripResponse(
        SimpleTripResponse trip,
        VehicleResponse vehicle,
        DriverProfileResponse driver,
        UserResponse createdBy

) {
}
