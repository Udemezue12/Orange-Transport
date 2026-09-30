package com.astrotech.transport.events;

import java.util.UUID;

public record SingleImageDeleteEvent(
        UUID profileId,
        String publicId,
        String resourceType
) {
}
