package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.RouteResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.Terminal;

public class RouteMapper {
    public static Route createRoute(Terminal originTerminal, Terminal destinationTerminal){


        return Route.builder()
                .originTerminal(originTerminal)
                .destinationTerminal(destinationTerminal)
                .build();
    }
    public static RouteResponse toResponse(Route route){
        var originTerminal = TerminalMapper.simpleResponse(route.getOriginTerminal());
        var destinationTerminal = TerminalMapper.simpleResponse(route.getDestinationTerminal());
        return new RouteResponse(
                route.getId(),
                route.getDistanceKm() != null ? route.getDistanceKm() : null,
                route.getEstimatedDurationMinutes() != null ? route.getEstimatedDurationMinutes() : null,
                originTerminal,
                destinationTerminal
        );
    }
}
