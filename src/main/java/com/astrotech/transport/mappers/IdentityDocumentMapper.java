package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.SimpleIdentityDocumentResponse;
import com.astrotech.transport.entities.IdentityDocument;
import com.astrotech.transport.entities.Profile;
import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.VerificationStatus;

import java.time.Instant;

public class IdentityDocumentMapper {

    public static IdentityDocument createDocument(Profile profile, DocumentType documentType,
                                                  String documentUrl,
                                                  String documentNumber,
                                                  String documentHash,
                                                  String documentAssetId,
                                                  String documentResourceType,
                                                  ImageUploadStatus uploadStatus,
                                                  String documentPublicId) {
        return IdentityDocument.builder()
                .profile(profile)
                .documentAssetId(documentAssetId)
                .documentNumber(documentNumber)
                .documentPublicId(documentPublicId)
                .documentHash(documentHash)
                .documentType(documentType)
                .documentUrl(documentUrl)
                .documentResourceType(documentResourceType)
                .uploadStatus(uploadStatus)
                .verificationStatus(VerificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();

    }
    public static SimpleIdentityDocumentResponse simpleResponse(IdentityDocument identityDocument) {
        return new SimpleIdentityDocumentResponse(
                identityDocument.getId(),
                identityDocument.getDocumentType(),
                identityDocument.getDocumentUrl(),
                identityDocument.getDocumentResourceType(),
                identityDocument.getDocumentPublicId(),
                identityDocument.getUploadStatus(),
                identityDocument.getVerificationStatus(),
                identityDocument.getCreatedAt()
        );
    }


}

