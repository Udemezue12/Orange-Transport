package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.request.VehicleRequest;
import com.astrotech.transport.dto.request.VehicleUpdateRequest;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.VehicleImageService;
import com.astrotech.transport.service.VehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "For creating, updating vehicles..Also for getting a vehicle or vehicles")
public class VehicleController {
    private final VehicleService vehicleService;
    private final ApiCacheControl apiCacheControl;

    private final VehicleImageService vehicleImageService;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(@Valid @RequestBody VehicleRequest vehicleRequest) {
        var response = vehicleService.createVehicle(vehicleRequest);
        return ApiResponseBuilder.success("Vehicle Created Successfully", response, apiCacheControl.noStore());
    }
    @PatchMapping("/{vehicleId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(@PathVariable UUID vehicleId, @Valid @RequestBody VehicleUpdateRequest request) {
        var response = vehicleService.updateVehicle(vehicleId, request);
        return ApiResponseBuilder.success("Vehicle Updated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<VehicleResponse>>> getAllVehicles(@RequestParam(required = false, defaultValue = "registrationNumber", name = "sortBy") String sortBy,
                                                                                      @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                      @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = vehicleService.getAllVehicles(page, size, sortBy);
        return ApiResponseBuilder.success("Vehicle Fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @GetMapping("/{vehicleId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<List<VehicleImageResponse>>> getVehicle(@PathVariable UUID vehicleId) {
        var response = vehicleImageService.getVehicleWithImages(vehicleId);
        return ApiResponseBuilder.success("Vehicle Fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @GetMapping("/{vehicleId}/get/seats/images")
    @Ratelimit
    public ResponseEntity<ApiResponse<VehicleWithImagesAndSeatsResponse>> getVehicleWithImagesAndSeats(@PathVariable UUID vehicleId) {
        var response = vehicleService.getVehicleWithImagesAndSeats(vehicleId);
        return ApiResponseBuilder.success("Vehicle Fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @GetMapping("/{vehicleId}/seats/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<VehicleWithSeatsResponse>> getVehicleWithSeats(@PathVariable UUID vehicleId) {
        var response = vehicleService.getVehicleWithSeats(vehicleId);
        return ApiResponseBuilder.success("Vehicle Fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }


    @GetMapping("/{vehicleId}/{vehicleImageId}/image")
    @Ratelimit
    public ResponseEntity<ApiResponse<UploadResponse>> getVehicleImage(@PathVariable UUID vehicleId, @PathVariable UUID vehicleImageId) {
        var response = vehicleImageService.getVehicleImage(vehicleId, vehicleImageId);
        return ApiResponseBuilder.success("Vehicle Image Fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }

    @DeleteMapping("/{vehicleId}/delete")
    @Ratelimit
    public Map<String, String> deleteVehicleImage(@PathVariable UUID vehicleId) {
        vehicleImageService.deleteVehicleImage(vehicleId);
        return Map.of("message", "Vehicle Image Deleted Successfully");
    }

}
