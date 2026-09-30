package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.request.RouteRequest;
import com.astrotech.transport.dto.request.RouteUpdateRequest;
import com.astrotech.transport.dto.response.RouteFareResponse;
import com.astrotech.transport.dto.response.RouteResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalRouteResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.RouteFareService;
import com.astrotech.transport.service.RouteService;
import com.astrotech.transport.service.TerminalRouteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/route")
@RequiredArgsConstructor
@Tag(name = "Route", description = "For creating, updating and getting routes")
public class RouteController {
    private final RouteService routeService;
    private final RouteFareService routeFareService;
    private final TerminalRouteService terminalRouteService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(@Valid @RequestBody RouteRequest request) {
        var response = routeService.createRoute(request);
        return ApiResponseBuilder.success("Route created successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("{routeId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(@PathVariable UUID routeId, @Valid RouteUpdateRequest request) {
        var response = routeService.updateRoute(request, routeId);
        return ApiResponseBuilder.success("Route updated successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{routeId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<TerminalRouteResponse>>> getTerminal(@PathVariable UUID routeId, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
                                                                                         @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                         @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = terminalRouteService.getRoutesTerminal(routeId, page, size, sortBy);
        return ApiResponseBuilder.success("Routes fetched Successfully", response, apiCacheControl.publicMaxAgeMinutes(5));


    }

    @GetMapping("/{routeId}/get/fare")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteFareResponse>> getRouteWithFare(@PathVariable UUID routeIdId) {
        var response = routeFareService.getRouteWithFare(routeIdId);
        return ApiResponseBuilder.success("Route fetched successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<RouteResponse>>> getRoutes(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                               @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = routeService.getAllRoutes(page, size, true);
        return ApiResponseBuilder.success("Routes fetched successfully", response, apiCacheControl.publicMaxAgeMinutes(5));

    }
    @GetMapping("/list")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<RouteResponse>>> getRouteList(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                               @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = routeService.getAllRoutes(page, size, false);
        return ApiResponseBuilder.success("Routes fetched successfully", response, apiCacheControl.publicMaxAgeMinutes(5));

    }

    @GetMapping("/{routeId}/fares")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<RouteFareResponse>>> getRouteWithFares(@PathVariable UUID routeId, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                           @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = routeFareService.getRouteWithFares(routeId, page, size);
        return ApiResponseBuilder.success("Routes fetched successfully", response, apiCacheControl.publicMaxAgeMinutes(5));

    }

}
