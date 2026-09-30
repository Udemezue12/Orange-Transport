package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.UnAssignedWorkerResponse;
import com.astrotech.transport.dto.response.UserResponse;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.*;
import com.astrotech.transport.service.DriverProfileService;
import com.astrotech.transport.service.ProfileService;
import com.astrotech.transport.service.UserService;
import com.astrotech.transport.validators.role.RoleRequired;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "for suspending, deleting, viewing and banning users")
public class UserController {
    private final UserService userService;
    private final ProfileService profileService;
    private final DriverProfileService driverProfileService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/{userId}/suspend")
    @Ratelimit
    public Map<String, String> suspendAccount(@PathVariable UUID userId) {
        return userService.suspendAccount(userId);
    }

    @PostMapping("/{userId}/restore")
    @Ratelimit

    public Map<String, String> restoreAccount(@PathVariable UUID userId) {
        return userService.restoreAccount(userId);
    }

    @PostMapping("/{userId}/delete")
    @Ratelimit
    @RoleRequired(UserRole.ADMIN)
    public Map<String, String> deleteAccount(@PathVariable UUID userId) {
        return userService.deleteAccount(userId);
    }

    @GetMapping("/{userId}")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        var response = userService.getUser(userId);
        return ApiResponseBuilder.success("User fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/search-users")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false, defaultValue = "fullName", name = "searchKeyWord") String searchKeyWord,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = userService.searchUsers(page, size, searchKeyWord);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserResponse>>> getUsers(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = userService.getUsers(page, size);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/terminal-supervisors")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserResponse>>> getTerminalSupervisors(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = userService.getAllTerminalSupervisors(page, size);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/unassigned-terminal-supervisors")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UserResponse>>> getUnassignedTerminalSupervisors(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = userService.getUnassignedTerminalSupervisors(page, size);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/unassigned-drivers")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UnAssignedWorkerResponse>>> getUnassignedDrivers(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = driverProfileService.getUnassignedDrivers(page, size);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/unassigned-workers")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<UnAssignedWorkerResponse>>> getOtherUnassignedWorkers(
            @RequestParam(name = "userRole") String userRole,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var response = profileService.getUnassignedWorkers(page, size, userRole);
        return ApiResponseBuilder.success("Users fetched successfully", response, apiCacheControl.noStore());
    }
}
