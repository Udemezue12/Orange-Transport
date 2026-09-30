package com.astrotech.transport.events;

import com.astrotech.transport.enums.OnlineStatus;

import java.time.Instant;

public record PresenceEvent(
        String userId,
        OnlineStatus status,
        Instant timestamp
) {
}
