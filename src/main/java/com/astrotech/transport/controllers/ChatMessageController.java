package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.response.ChatMessageResponse;
import com.astrotech.transport.dto.response.SendChatMessageRequest;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.ChatMessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat-message")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "For getting messages, also for sending messages over the http")
public class ChatMessageController {
    private final ChatMessageService messageService;
    private final ApiCacheControl apiCacheControl;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/{conversationId}/sendMessage")
    @Ratelimit
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(@PathVariable UUID conversationId, @Valid @RequestBody SendChatMessageRequest request) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = messageService.createMessage(conversationId, userId, request);
        return ApiResponseBuilder.success(
                "Message sent successfully",
                response,
                apiCacheControl.noStore()
        );
    }

    @GetMapping("/{messageId}/get-message")
    @Ratelimit
    public ResponseEntity<ApiResponse<ChatMessageResponse>> getMessage(@PathVariable String messagedId) {

        var response = messageService.getMessageById(messagedId);
        return ApiResponseBuilder.success(
                "Message sent successfully",
                response,
                apiCacheControl.noStore()
        );
    }


}

