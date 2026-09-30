package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.AssignCustomerAgentRequest;
import com.astrotech.transport.dto.response.AssignAgentConversationResponse;
import com.astrotech.transport.dto.response.ChatMessageResponse;
import com.astrotech.transport.dto.response.SimpleChatConversationResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.ChatConversationService;
import com.astrotech.transport.service.ChatMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat-conversation")
@RequiredArgsConstructor
@Tag(name = "Chat Conversation", description = "For getting conversations and also getting chat conversations with messages")
public class ChatConversationController {
    private final ChatConversationService conversationService;
    private final ChatMessageService messageService;
    private final ApiCacheControl apiCacheControl;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleChatConversationResponse>> createConversation() {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = conversationService.createChatConversation(userId);
        return ApiResponseBuilder.success("Conversation created Successfully", response, apiCacheControl.noStore());
    }

    @PutMapping("/{conversationId}/assign")
    @Ratelimit
    public ResponseEntity<ApiResponse<AssignAgentConversationResponse>> assignCustomerServiceConversationAgent(@PathVariable UUID conversationId, @Valid @RequestBody AssignCustomerAgentRequest request) {

        var response = conversationService.assignAgent(request.userId(), conversationId);
        return ApiResponseBuilder.success("Customer Service Agent assigned Successfully", response, apiCacheControl.noStore());
    }

    @PostMapping("/{conversationId}/close")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleChatConversationResponse>> closeConversation(@PathVariable UUID conversationId) {

        var response = conversationService.closeConvo(conversationId);
        return ApiResponseBuilder.success("Conversation closed Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/user")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleChatConversationResponse>>> getAllConversationByUser(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = conversationService.findConvosByPassenger(userId, page, size);
        return ApiResponseBuilder.success("Conversations fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleChatConversationResponse>>> getAllConversations(
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {

        var response = conversationService.findAllConversations(page, size);
        return ApiResponseBuilder.success("Conversations fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{conversationId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<ChatMessageResponse>>> getConversation(
            @PathVariable UUID conversationId,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size
    ) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var role = getCurrentUser.getCurrentUserIdAndRole().role();
        var response = messageService.getMessages(conversationId, userId, role, page, size);
        return ApiResponseBuilder.success("Conversations fetched Successfully", response, apiCacheControl.noStore());
    }

}


