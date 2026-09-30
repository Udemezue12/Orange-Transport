package com.astrotech.transport.dto.response;

public record UploadResponse(
        String id,
        String secureUrl,
        String publicId,
        String resourceType
) {
}
