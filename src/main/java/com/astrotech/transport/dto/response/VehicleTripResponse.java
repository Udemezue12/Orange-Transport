package com.astrotech.transport.dto.response;

public record VehicleTripResponse(
        SimpleTripResponse trip,
        RouteResponse route,
        DriverProfileResponse driver,
        UserResponse createdBy
){
    
    
}
