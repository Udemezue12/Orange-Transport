package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;

import com.astrotech.transport.dto.request.RouteFareRequest;
import com.astrotech.transport.dto.request.RouteFareUpdateRequest;
import com.astrotech.transport.dto.response.RouteFareResponse;
import com.astrotech.transport.dto.response.SimpleRouteFareResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.RouteFareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/route-fare")
@RequiredArgsConstructor
@Tag(name = "Route Fare", description = "For creating, updating fares to routes..Also for viewing fares for routes")
public class RouteFareController {
    private final RouteFareService routeFareService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/create/{routeId}")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteFareResponse>> createRouteFare(@Valid @RequestBody RouteFareRequest routeFareRequest, @PathVariable UUID routeId) {
        var response = routeFareService.createRouteFare(routeFareRequest, routeId);
        return ApiResponseBuilder.success("Fare added successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/{routeFareId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleRouteFareResponse>> updateRouteFare(@Valid @RequestBody RouteFareUpdateRequest routeFareRequest, @PathVariable UUID routeFareId) {
        var response = routeFareService.updateRouteFare(routeFareRequest, routeFareId);
        return ApiResponseBuilder.success("Fare updated successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{routeFareId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteFareResponse>> getFare(@PathVariable UUID routeFareId) {
        var response = routeFareService.getRouteFare(routeFareId);
        return ApiResponseBuilder.success("Fare fetched successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleRouteFareResponse>>> getAllFares(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                           @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = routeFareService.getAllFares(page, size);
        return ApiResponseBuilder.success("Fare fetched successfully", response, apiCacheControl.publicMaxAgeMinutes(5));
    }
}
