package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.dto.request.RescueAndTransloadRequest;
import com.astrotech.transport.entities.TripVehicleAllocation;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TripVehicleAllocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trip-vehilce")
@RequiredArgsConstructor
@Tag(name = "Trip Vehicle Transloading", description = "Endpoints for managing and retrieving vehicle transloading activities, cargo transfers, and trip schedules")
public class TripVehicleTransloadingController {

    private final TripVehicleAllocationService tripVehicleAllocationService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/transload")
    public ResponseEntity<ApiResponse<TripVehicleAllocation>> transloadVehicle(
            @Valid @RequestBody RescueAndTransloadRequest request
    ) {
        var response = tripVehicleAllocationService.transloadVehicle(request);

        return ApiResponseBuilder.success(
                "Vehicle and Passengers Transloaded Successfully",
                response,
                apiCacheControl.noStore()
        );
    }
}
