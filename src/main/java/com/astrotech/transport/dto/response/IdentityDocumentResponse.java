package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record IdentityDocumentResponse(
        SimpleProfileResponse profileResponse,
        SimpleIdentityDocumentResponse documentResponse,
        UserResponse userResponse
){


}

