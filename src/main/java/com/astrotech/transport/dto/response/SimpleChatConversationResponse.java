package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.ConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record SimpleChatConversationResponse(
        UUID id,
        ConversationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}

