package com.astrotech.transport.dto.response;



public record RouteFareResponse(
        RouteResponse route,
        SimpleRouteFareResponse routeFare
) {

}
