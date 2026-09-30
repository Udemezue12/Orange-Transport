package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.ChatMessageType;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        ChatMessageType type,
        String content,
        boolean sent,
        Instant createdAt
) {
}
