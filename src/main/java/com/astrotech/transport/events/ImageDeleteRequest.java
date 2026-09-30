package com.astrotech.transport.events;

public record ImageDeleteRequest(
        String resourceType,
        String publicId) {
}
