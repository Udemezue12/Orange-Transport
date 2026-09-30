package com.astrotech.transport.events;

import java.util.UUID;

public record ProfilePicUploadEvent(
        UUID userId,
        String assetId,
        String publicId
){}