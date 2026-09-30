package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.dto.request.BulkUpdateSeatsRequest;
import com.astrotech.transport.dto.request.CreateSeatRequest;
import com.astrotech.transport.dto.response.SeatResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.SeatService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicle/seat")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "For adding and updating vehicle seats..")
public class SeatController {
    private final SeatService seatService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/{vehicleId}/create")
    @Ratelimit
    @Hidden
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createSeat(@Valid @RequestBody CreateSeatRequest request, @PathVariable UUID vehicleId) {
        var response = seatService.createSeat(vehicleId, request);
        return ApiResponseBuilder.success("Seats Created Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/{vehicleId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<List<SeatResponse>>> updateSeat(@Valid @RequestBody BulkUpdateSeatsRequest request, @PathVariable UUID vehicleId) {
        var response = seatService.updateSeats(vehicleId, request);
        return ApiResponseBuilder.success("Seats Updated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{vehicleId}/get")
    @Ratelimit
    @Hidden
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getVehicleSeats(@PathVariable UUID vehicleId) {
        var response = seatService.getVehicleSeats(vehicleId);
        return ApiResponseBuilder.success("Seats fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

}
