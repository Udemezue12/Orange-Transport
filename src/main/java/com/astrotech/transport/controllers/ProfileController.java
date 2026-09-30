package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.CreateProfileRequest;
import com.astrotech.transport.dto.request.UserUpdate;
import com.astrotech.transport.dto.response.ProfileResponse;
import com.astrotech.transport.dto.response.SimpleIdentityDocumentResponse;
import com.astrotech.transport.dto.response.SimpleProfileResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.IdentityDocumentService;
import com.astrotech.transport.service.ProfileService;
import com.astrotech.transport.validators.role.RoleRequired;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "For users to view their profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final GetCurrentUser getCurrentUser;
    private final IdentityDocumentService identityDocumentService;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/profile/create")
    @Ratelimit
    @Hidden
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = profileService.createProfile(currentUserId, request);
        return ApiResponseBuilder.success("Profile Created Successfully", response, apiCacheControl.noStore());
    }

    @PatchMapping("/profile/update")
    @Ratelimit
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody UserUpdate request) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = profileService.updateProfile(request, currentUserId);
        return ApiResponseBuilder.success("Profile Updated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/profile/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = profileService.getProfile(currentUserId);
        return ApiResponseBuilder.success("Profile Fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/profile/{userId}/get-user")
    @Ratelimit
    @RoleRequired({UserRole.ADMIN,
            UserRole.TERMINAL_SUPERVISOR,
            UserRole.CUSTOMER_SERVICE_AGENT,
            UserRole.CASHIER
    })
    public ResponseEntity<ApiResponse<ProfileResponse>> getUserProfile(@PathVariable UUID userId) {

        var response = profileService.getProfile(userId);
        return ApiResponseBuilder.success("Profile Fetched Successfully", response, apiCacheControl.noStore());
    }


    @GetMapping("/profiles/search-all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleProfileResponse>>> searchForProfiles(
            @RequestParam(name = "searchName") String searchName,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {

        var response = profileService.searchActiveProfiles(searchName, page, size);
        return ApiResponseBuilder.success("Profile Fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/profiles/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleProfileResponse>>> getProfiles(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {

        var response = profileService.getProfiles(page, size);
        return ApiResponseBuilder.success("Profile Fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/profile/{documentId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleIdentityDocumentResponse>> getProfileDocument(@PathVariable UUID documentId) {

        var response = identityDocumentService.getDocument(documentId);
        return ApiResponseBuilder.success("Document Fetched Successfully", response, apiCacheControl.noStore());
    }
}
