package com.astrotech.transport.events;

public record ProfilePicUpdateRequest(
        String Id,
        String oldPublicId,
        String oldResourceType,
        String newAssetId,
        String newPublicId

) {
}
