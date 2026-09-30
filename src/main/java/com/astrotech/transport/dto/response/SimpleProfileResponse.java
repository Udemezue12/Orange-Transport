package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.VerificationStatus;

import java.util.UUID;

public record SimpleProfileResponse(
        UUID id,
        ImageUploadStatus uploadStatus,
        String profilePicUrl,
        String profilePicPublicId,
        String profilePicResourceType
){
    
}
