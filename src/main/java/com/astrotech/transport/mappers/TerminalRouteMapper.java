package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.request.TerminalRouteRequest;
import com.astrotech.transport.dto.response.SimpleTerminalRouteResponse;
import com.astrotech.transport.dto.response.TerminalRouteResponse;
import com.astrotech.transport.entities.Route;
import com.astrotech.transport.entities.Terminal;
import com.astrotech.transport.entities.TerminalRoute;

import java.time.Instant;

public class TerminalRouteMapper {
    public static TerminalRoute createTerminalRoute(TerminalRouteRequest request, Route route, Terminal terminal) {
        var finalOrderCreatedAt = request.stopOrder() != null
                ? Instant.now()
                : null;
        return TerminalRoute.builder()
                .stopOrderCreatedAt(finalOrderCreatedAt)
                .stopOrder(request.stopOrder())
                .route(route)
                .terminal(terminal)
                .build();
    }

    public static SimpleTerminalRouteResponse toSimpleTerminalRouteResponse(TerminalRoute terminalRoute) {
        var finalOrder = terminalRoute.getStopOrder() != null
                ? terminalRoute.getStopOrder()
                : null;
        return new SimpleTerminalRouteResponse(
                terminalRoute.getId(),
                finalOrder,
                terminalRoute.getStopOrderCreatedAt()
        );
    }

    public static TerminalRouteResponse toTerminalRouteResponse(TerminalRoute terminalRoute) {
        var routeResponse = RouteMapper.toResponse(terminalRoute.getRoute());
        var terminalResponse = TerminalMapper.toResponse(terminalRoute.getTerminal());
        var terminalRouteResponse = toSimpleTerminalRouteResponse(terminalRoute);
        return new TerminalRouteResponse(
                terminalRouteResponse,
                terminalResponse,
                routeResponse


        );
    }
}
