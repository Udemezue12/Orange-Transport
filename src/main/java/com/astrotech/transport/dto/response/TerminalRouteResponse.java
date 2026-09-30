package com.astrotech.transport.dto.response;


public record TerminalRouteResponse(
        SimpleTerminalRouteResponse terminalRouteResponse,
        TerminalResponse terminalResponse,
        RouteResponse routeResponse

) {
}
