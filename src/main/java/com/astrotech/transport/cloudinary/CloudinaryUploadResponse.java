package com.astrotech.transport.cloudinary;

public record CloudinaryUploadResponse(
        String assetId,
        String resourceType,
        String secureUrl,
        String publicId
) {}
