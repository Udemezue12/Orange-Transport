package com.astrotech.transport.events;

public record ImageUploadRequest(
        String Id,
        String assetId,
        String publicId
) {
}
