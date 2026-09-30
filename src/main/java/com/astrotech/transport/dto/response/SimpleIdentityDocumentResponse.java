package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record SimpleIdentityDocumentResponse(
        UUID id,
        DocumentType documentType,
        String documentUrl,
        String documentResourceType,
        String documentPublicId,
        ImageUploadStatus uploadStatus,
        VerificationStatus verificationStatus,
        Instant createdAt
        
){
    
}
