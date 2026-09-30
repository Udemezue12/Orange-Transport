package com.astrotech.transport.events;

import java.util.List;

public record VehicleImageUploadRequest(
        String Id,
        String assetId,
        String publicId) {
}
