package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.request.RouteFareRequest;
import com.astrotech.transport.dto.response.RouteFareResponse;
import com.astrotech.transport.dto.response.SimpleRouteFareResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.RouteFare;

public class RouteFareMapper {
    public static RouteFare createRouteFare(RouteFareRequest request, Route route){
        return RouteFare.builder()
                .route(route)
                .active(true)
                .amount(request.amount())
                .effectiveTo(request.effectiveTo())
                .vehicleClass(request.vehicleClass())
                .effectiveFrom(request.effectiveFrom())
                .build();

    }

    public static RouteFareResponse routeFareResponse(RouteFare routeFare){
        var routeResponse = RouteMapper.toResponse(routeFare.getRoute());
        var simpleRouteResponse  = simpleRouteFareResponse(routeFare);
        return new RouteFareResponse(
                routeResponse,
                simpleRouteResponse
        );


    }
    public static SimpleRouteFareResponse simpleRouteFareResponse(RouteFare routeFare){
        return new SimpleRouteFareResponse(
                routeFare.getId(),
                routeFare.getAmount(),
                routeFare.getEffectiveFrom(),
                routeFare.getEffectiveTo()
        );
    }
}
