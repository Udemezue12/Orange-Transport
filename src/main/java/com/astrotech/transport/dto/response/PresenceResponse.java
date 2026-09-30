package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.ChatMessageType;
import com.astrotech.transport.enums.OnlineStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record PresenceResponse(
         String userId,
        OnlineStatus status,
         Instant lastSeen
) {
}


