package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.response.SimpleAssignedWorkerResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TerminalAssignedWorkerResponse;
import com.astrotech.transport.dto.response.TripAssignedWorkerResponse;
import com.astrotech.transport.enums.AssignedServiceType;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.AssignWorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assigned-workers")
@RequiredArgsConstructor
@Tag(name = "Assigned Workers", description = "For getting workers and their respective assignments")
public class AssignWorkerController {
    private final AssignWorkerService assignWorkerService;
    private final ApiCacheControl apiCacheControl;

    @GetMapping
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleAssignedWorkerResponse>>> getAllAssignedWorkers(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size,
            @RequestParam(required = false, defaultValue = "createdAt", name = "sortBy") String sortBy) {
        var response = assignWorkerService.getAllAssignedWorkers(page, size, sortBy);
        return ApiResponseBuilder.success("Assigned Workers fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{serviceId}/trip-driver")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripAssignedWorkerResponse>> getAssignedTripDriver(@PathVariable UUID serviceId) {
        var response = assignWorkerService.getTripAssignedWorker(serviceId, AssignedServiceType.TRIP_VEHICLE_DRIVER);
        return ApiResponseBuilder.success("Assigned Worker fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{serviceId}/trip-driver")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripAssignedWorkerResponse>> getAssignedTripLoader(@PathVariable UUID serviceId) {
        var response = assignWorkerService.getTripAssignedWorker(serviceId, AssignedServiceType.TRIP_VEHICLE_LOADER);
        return ApiResponseBuilder.success("Assigned Worker fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{serviceId}/terminal")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalAssignedWorkerResponse>> getAssignedTerminalWorker(@PathVariable UUID serviceId) {
        var response = assignWorkerService.getTerminalAssignedWorker(serviceId, AssignedServiceType.TERMINAL);
        return ApiResponseBuilder.success("Assigned Worker fetched Successfully", response, apiCacheControl.noStore());
    }

}
