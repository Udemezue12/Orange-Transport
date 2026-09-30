package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.DriverProfileRequest;
import com.astrotech.transport.dto.request.DriverProfileUpdateRequest;
import com.astrotech.transport.dto.response.DriverProfileResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.DriverProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver-profile")
@RequiredArgsConstructor
@Tag(name = "Driver Profile", description = "For drivers to create, update and view their profiles")
public class DriverProfileController {
    private final DriverProfileService driverProfileService;
    private final GetCurrentUser getCurrentUser;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<DriverProfileResponse>> createProfile(@Valid @RequestBody DriverProfileRequest request) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = driverProfileService.createProfile(request, currentUserId);
        return ApiResponseBuilder.success("Profile Created Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<DriverProfileResponse>> updateProfile(@Valid @RequestBody DriverProfileUpdateRequest request) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = driverProfileService.updateProfile(request, currentUserId);
        return ApiResponseBuilder.success("Profile Updated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<DriverProfileResponse>>> getAllProfiles(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var response = driverProfileService.getAllDrivers(page, size, sortBy);
        return ApiResponseBuilder.success("All Profiles Loaded Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<DriverProfileResponse>> getProfile() {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = driverProfileService.getSingleProfile(userId);
        return ApiResponseBuilder.success("Profile Loaded Successfully", response, apiCacheControl.noStore());

    }
    @GetMapping("/{driverId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<DriverProfileResponse>> getDriverProfile(@PathVariable UUID driverId) {

        var response = driverProfileService.getSingleProfile(driverId);
        return ApiResponseBuilder.success("Profile Loaded Successfully", response,apiCacheControl.noStore());

    }


}
