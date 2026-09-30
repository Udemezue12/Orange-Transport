package com.astrotech.transport.dto.response;

public record TerminalSupervisorTripResponse(
        SimpleTripResponse trip,
        VehicleResponse vehicle,
        RouteResponse route,
        DriverProfileResponse driver
){
    
    
}
