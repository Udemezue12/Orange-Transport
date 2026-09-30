package com.astrotech.transport.controllers;


import com.astrotech.transport.cloudinary.CloudinaryImageDeletionRequest;
import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.UserRequest;
import com.astrotech.transport.dto.response.ChatMessageResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.UserResponse;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.AuthService;
import com.astrotech.transport.service.ChatMessageService;
import com.astrotech.transport.service.IdentityDocumentService;
import com.astrotech.transport.validators.role.RoleRequired;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {
    private final AuthService authService;
    private final CloudinaryService cloudinaryService;
    private final ApiCacheControl apiCacheControl;
    private final IdentityDocumentService documentService;
    private final ChatMessageService messageService;
    private final GetCurrentUser getCurrentUser;


    @PostMapping("/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> admin_register(@Valid @RequestBody UserRequest userRequest) {
        return authService.admin_register(userRequest);
    }

    @PostMapping("/cloudinary-delete")
    @Ratelimit
    @RoleRequired(UserRole.ADMIN)
    public ResponseEntity<ApiResponse<Boolean>> delete(@Valid @RequestBody CloudinaryImageDeletionRequest request) {
        var response = cloudinaryService.deleteResources(request.publicIds());
        return ApiResponseBuilder.success("Deleted successfully", response, apiCacheControl.noStore());
    }

    @PostMapping("/{documentId}/approve-document")
    @Ratelimit
    public ResponseEntity<ApiResponse<Void>> approveDocument(@PathVariable UUID documentId) {
        documentService.approveDocument(documentId);
        return ApiResponseBuilder.success("Document approved successfully", null, apiCacheControl.noStore());
    }

    @PostMapping("/{documentId}/decline-document")
    @Ratelimit
    public ResponseEntity<ApiResponse<Void>> declineDocument(@PathVariable UUID documentId) {
        documentService.declineDocument(documentId);
        return ApiResponseBuilder.success("Document Rejected successfully", null, apiCacheControl.noStore());
    }

    @GetMapping("/{conversationId}/{userId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<ChatMessageResponse>>> getConversation(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {

        var role = getCurrentUser.getCurrentUserIdAndRole().role();
        var response = messageService.getMessages(conversationId, userId, role, page, size);
        return ApiResponseBuilder.success("Conversations fetched Successfully", response, apiCacheControl.noStore());
    }


}
