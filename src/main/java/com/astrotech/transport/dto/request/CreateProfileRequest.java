package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.DocumentType;
import jakarta.validation.constraints.*;

public record CreateProfileRequest(


        String profilePicAssetId,


        String profilePicPublicId,


        DocumentType documentType,


        @Size(min = 3, max = 50, message = "Document number must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "Document number must contain only alphanumeric characters and hyphens")
        String documentNumber,


        String documentAssetId,

        String documentPublicId
) {
}
