package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.UploadResponse;

import java.util.UUID;


public class UploadMapper {
    public static UploadResponse toUploadResponse(String imageUrl, String publicId, String resourceType, UUID imageId) {
        return new UploadResponse(
                String.valueOf(imageId),
                imageUrl,
                publicId,
                resourceType
        );
    }


}
