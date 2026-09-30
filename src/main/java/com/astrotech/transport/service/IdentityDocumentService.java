package com.astrotech.transport.service;

import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.SimpleIdentityDocumentResponse;
import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.VerificationStatus;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.IdentityDocumentMapper;
import com.astrotech.transport.repositories.IdentityDocumentRepository;
import com.astrotech.transport.repositories.ProfileRepository;
import com.astrotech.transport.utilities.hash.RequestHashUtil;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IdentityDocumentService {
    private final IdentityDocumentRepository identityDocumentRepository;
    private final RequestHashUtil requestHashUtil;
    private final ProfileRepository profileRepository;

    @Transactional
    @CacheEvict(value = "profile-documents", allEntries = true)
    public void saveDocument(UUID userId,
                             DocumentType documentType,
                             String documentUrl,
                             String documentNumber,
                             String documentAssetId,
                             String documentResourceType,
                             ImageUploadStatus uploadStatus,
                             String documentPublicId) {
        validateObjectRequest(userId,
                documentType,
                documentUrl,
                documentNumber,
                documentAssetId,
                documentResourceType,
                uploadStatus,
                documentPublicId);
        var profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return;
        }
        var documentHash = requestHashUtil.hash(documentAssetId);
        var documentMapper = IdentityDocumentMapper.createDocument(profile, documentType, documentUrl,
                documentNumber, documentHash, documentAssetId, documentResourceType, uploadStatus, documentPublicId);
        identityDocumentRepository.save(documentMapper);


    }
    @CustomCacheable(
            value = "profile-documents",
            key = "#documentId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public SimpleIdentityDocumentResponse getDocument(UUID documentId) {
        return identityDocumentRepository.findById(documentId)
                .map(IdentityDocumentMapper::simpleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No Document Found"));
    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)

    @Caching(evict = {
            @CacheEvict(value = "user-profiles", allEntries = true),
            @CacheEvict(value = "profile-documents", allEntries = true)
    })
    public void approveDocument(UUID documentId) {
        updateDocumentVerificationStatus(
                documentId,
                VerificationStatus.APPROVED
        );
    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "user-profiles", allEntries = true),
            @CacheEvict(value = "profile-documents", allEntries = true)
    })
    public void declineDocument(UUID documentId) {
        updateDocumentVerificationStatus(
                documentId,
                VerificationStatus.DECLINED
        );
    }

    private void updateDocumentVerificationStatus(
            UUID documentId,
            VerificationStatus status
    ) {
        var profile = profileRepository
                .findByIdentityDocumentId(documentId)
                .orElse(null);

        if (profile == null || profile.getIdentityDocument() == null) {
            return;
        }

        profile.getIdentityDocument()
                .setVerificationStatus(status);
    }


    private static void validateObjectRequest(UUID userId, DocumentType documentType,
                                              String documentUrl, String documentNumber, String documentAssetId, String documentResourceType, ImageUploadStatus uploadStatus, String documentPublicId) {
        Objects.requireNonNull(userId, "UserId Id cannot be null");
        Objects.requireNonNull(documentType, "Document Type cannot be null");
        Objects.requireNonNull(documentUrl, "Document URL cannot be null");
        Objects.requireNonNull(documentNumber, "Document Number cannot be null");

        Objects.requireNonNull(documentAssetId, "Document Asset ID cannot be null");
        Objects.requireNonNull(documentResourceType, "Document Resource Type cannot be null");
        Objects.requireNonNull(uploadStatus, "Upload Status cannot be null");
        Objects.requireNonNull(documentPublicId, "Document Public ID cannot be null");
    }


}
