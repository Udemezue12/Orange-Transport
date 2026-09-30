package com.astrotech.transport.events;

import com.astrotech.transport.enums.DocumentType;

import java.util.UUID;

public record DocumentUploadEvent(
        UUID userId,
        String assetId,
        String publicId,
        DocumentType documentType,
        String documentNumber
) {
}
