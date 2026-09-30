package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TripService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trip")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "For creating, updating and getting trip/trips")
public class TripController {
    private final TripService tripService;
    private final GetCurrentUser getCurrentUser;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripResponse>> create(@Valid @RequestBody TripRequest request) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = tripService.createTrip(request, currentUserId);
        return ApiResponseBuilder.success("Trip Created Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/{tripId}/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripResponse>> update(@PathVariable UUID tripId, @Valid @RequestBody TripUpdateRequest request) {

        var response = tripService.updateTrip(tripId, request);
        return ApiResponseBuilder.success("Trip Updated Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/{tripId}/update/status")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleTripResponse>> updateTripStatus(@PathVariable UUID tripId, @Valid @RequestBody UpdateTripStatus request) {

        var response = tripService.updateTripStatus(tripId, request);
        return ApiResponseBuilder.success("Trip Updated Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/{tripId}/update/status/reason")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleTripResponse>> updateTripStatusWithReason(@PathVariable UUID tripId, @Valid @RequestBody UpdateTripDelayWithReason request) {

        var response = tripService.updateTripStatusWithReason(tripId, request);
        return ApiResponseBuilder.success("Trip Updated Successfully", response, apiCacheControl.noStore());
    }


    @GetMapping("/{tripCode}/trip-code")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripCodeResponse>> getTripCode(@PathVariable String tripCode) {
        var response = tripService.getByTripCode(tripCode);
        return ApiResponseBuilder.success("Trip fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/get/user")
    @Ratelimit
    public ResponseEntity<ApiResponse<TerminalSupervisorTripResponse>> getTripByUser() {
        var currentUser = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = tripService.getTripByUser(currentUser);
        return ApiResponseBuilder.success("Trip fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/get/{vehicleId}/vehicle")
    @Ratelimit
    public ResponseEntity<ApiResponse<VehicleTripResponse>> getTripByVehicle(@PathVariable UUID vehicleId) {
        var response = tripService.getTripByVehicleId(vehicleId);
        return ApiResponseBuilder.success("Trip fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/get/{routeId}/route")
    @Ratelimit
    public ResponseEntity<ApiResponse<RouteTripResponse>> getTripByRoute(@PathVariable UUID routeId) {
        var response = tripService.getTripByRouteId(routeId);
        return ApiResponseBuilder.success("Trip fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/get/{driverId}/driver")
    @Ratelimit
    public ResponseEntity<ApiResponse<DriverTripResponse>> getTripByDriver(@PathVariable UUID driverId) {
        var response = tripService.getTripByDriverId(driverId);
        return ApiResponseBuilder.success("Trip fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripResponse>>> getAll(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                 @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var response = tripService.getAllTrips(page, size, sortBy);
        return ApiResponseBuilder.success("Trips fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/vehicle/{vehicleId}/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripResponse>>> getAllTripsByVehicle(
            @PathVariable UUID vehicleId,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size,
            @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var response = tripService.getAllTripsByVehicleId(vehicleId, page, size, sortBy);
        return ApiResponseBuilder.success("Trips fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/route/{routeId}/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripResponse>>> getAllTripsByRoute(@PathVariable UUID routeId, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                             @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var response = tripService.getAllTripsByRouteId(routeId, page, size, sortBy);
        return ApiResponseBuilder.success("Trips fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/driver/{driverId}/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripResponse>>> getAllTripsByDriver(@PathVariable UUID driverId, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                              @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var response = tripService.getAllTripsByDriverId(driverId, page, size, sortBy);
        return ApiResponseBuilder.success("Trips fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/search")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripResponse>>> searchTrips(@Valid @RequestBody TripSearchRequest request, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                      @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = tripService.searchTrips(request, request.travelDate(), page, size);
        return ApiResponseBuilder.success("Trips fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));
    }
}
