package com.astrotech.transport.cloudinary;

import lombok.Builder;

@Builder
public record VerifiedCloudinaryAsset(
        String assetId,
        String thumbNailUrl,
        String publicId,
        String secureUrl,
        String resourceType,
        Long bytes,
        Integer width,
        Integer height,
        String format

) {}
