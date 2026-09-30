package com.astrotech.transport.events;

public record ImageUpdateRequest(
        String Id,
        String oldPublicId,
        String oldResourceType,
        String newAssetId,
        String newPublicId

) {
}
