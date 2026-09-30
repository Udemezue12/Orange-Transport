package com.astrotech.transport.dto.response;

public record TripWithoutUserResponse(
        SimpleTripResponse tripResponse,
        RouteResponse routeResponse,
        DriverProfileResponse driverResponse) {
}
