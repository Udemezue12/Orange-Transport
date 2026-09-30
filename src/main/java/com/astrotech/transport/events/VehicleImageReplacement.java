package com.astrotech.transport.events;

import java.util.UUID;

public record VehicleImageReplacement(
        UUID oldImageId,
        String newAssetId,
        String newPublicId
) {}
