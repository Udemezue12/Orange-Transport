package com.astrotech.transport.events;

import java.time.Instant;
import java.util.UUID;

public record TypingEvent(
        UUID id,
        String name,
        String conversationId,
        boolean isTyping,
        Instant now
) {
}

