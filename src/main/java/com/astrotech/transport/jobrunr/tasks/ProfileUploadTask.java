package com.astrotech.transport.jobrunr.tasks;



import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.ResourceType;
import com.astrotech.transport.service.DriverProfileService;
import com.astrotech.transport.service.IdentityDocumentService;
import com.astrotech.transport.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProfileUploadTask {
    private final DriverProfileService driverProfileService;

    private final CloudinaryService cloudinaryService;
    private final IdentityDocumentService identityDocumentService;
    private final ProfileService profileService;



    @Job(name = "identity-document-uploads", retries = 3)
    public void saveDocument(
            UUID userId,
            DocumentType documentType,
            String documentNumber,
            String assetId,
            String publicId
    ) {
        var asset = cloudinaryService.getVerifiedCloudinaryAsset(publicId, assetId);

        if (ResourceType.RAW.name().equalsIgnoreCase(asset.resourceType())) {
            identityDocumentService.saveDocument(
                    userId,
                    documentType,
                    asset.secureUrl(),
                    documentNumber,
                    asset.assetId(),
                    asset.resourceType(),
                    ImageUploadStatus.VERIFIED,
                    asset.publicId()
            );
            return;
        }

        var deleted = cloudinaryService.deleteResource(
                asset.publicId(),
                asset.resourceType()
        );

        if (!deleted) {
            log.warn(
                    "Failed to delete invalid Cloudinary document. userId={}, assetId={}, publicId={}, resourceType={}",
                    userId,
                    asset.assetId(),
                    asset.publicId(),
                    asset.resourceType()
            );
        }

        identityDocumentService.saveDocument(
                userId,
                documentType,
                null,
                documentNumber,
                null,
                null,
                ImageUploadStatus.FAILED,
                null
        );
    }

    @Job(name = "profile-pic-uploads", retries = 3)
    public void uploadProfilePic(
            String userId,
            String publicId,
            String assetId
    ) {
        log.info(
                "Starting Cloudinary profile pic verification. userId={}, assetId={}",
                userId,
                assetId
        );

        var userUuid = UUID.fromString(userId);

        var asset = cloudinaryService.getVerifiedCloudinaryAsset(
                publicId,
                assetId
        );

        var expectedResourceType = ResourceType.IMAGE.name();
        var responseResourceType = asset.resourceType();

        if (!expectedResourceType.equalsIgnoreCase(responseResourceType)) {

            var deleted = cloudinaryService.deleteResource(
                    asset.publicId(),
                    responseResourceType
            );

            if (!deleted) {
                log.warn(
                        "Failed to delete invalid profile pic from Cloudinary. " +
                                "userId={}, assetId={}, publicId={}, resourceType={}",
                        userId,
                        asset.assetId(),
                        asset.publicId(),
                        responseResourceType
                );
            }

            driverProfileService.updateProfileImage(
                    userUuid,
                    null,
                    null,
                    null,
                    null,
                    ImageUploadStatus.FAILED
            );

            log.warn(
                    "Cloudinary profile image rejected. " +
                            "userId={}, expectedResourceType={}, actualResourceType={}",
                    userId,
                    expectedResourceType,
                    responseResourceType
            );

            return;
        }

        profileService.updateProfilePicImage(
                userUuid,
                asset.publicId(),
                asset.assetId(),
                asset.secureUrl(),
                responseResourceType,
                ImageUploadStatus.VERIFIED
        );

        log.info(
                "Cloudinary profile pic verification completed. userId={}, assetId={}",
                userId,
                asset.assetId()
        );
    }
    @Job(name = "profile-pic-update-uploads", retries = 3)
    public void updateProfilePic(
            String userId,
            String oldPublicId,
            String oldResourceType,
            String newAssetId,
            String newPublicId
    ) {
        log.info(
                "Starting profile image update. userId={}, newAssetId={}",
                userId,
                newAssetId
        );

        var userUuid = UUID.fromString(userId);


        var asset = cloudinaryService.getVerifiedCloudinaryAsset(
                newPublicId,
                newAssetId
        );

        var expectedResourceType = ResourceType.IMAGE.name();
        var responseResourceType = asset.resourceType();


        if (!expectedResourceType.equalsIgnoreCase(responseResourceType)) {

            var deleted = cloudinaryService.deleteResource(
                    asset.publicId(),
                    responseResourceType
            );

            if (!deleted) {
                log.warn(
                        "Failed to delete invalid replacement profile image. " +
                                "userId={}, assetId={}, publicId={}, resourceType={}",
                        userId,
                        asset.assetId(),
                        asset.publicId(),
                        responseResourceType
                );
            }

            profileService.updateProfilePicImage(
                    userUuid,
                    null,
                    null,
                    null,
                    null,
                    ImageUploadStatus.FAILED
            );

            log.warn(
                    "Profile image replacement rejected. " +
                            "userId={}, expectedResourceType={}, actualResourceType={}",
                    userId,
                    expectedResourceType,
                    responseResourceType
            );

            return;
        }


        profileService.updateProfilePicImage(
                userUuid,
                asset.publicId(),
                asset.assetId(),
                asset.secureUrl(),
                responseResourceType,
                ImageUploadStatus.VERIFIED
        );


        var deleted = cloudinaryService.deleteResource(
                oldPublicId,
                oldResourceType
        );

        if (!deleted) {
            log.warn(
                    "New profile image saved, but failed to delete old Cloudinary image. " +
                            "userId={}, oldPublicId={}, newPublicId={}",
                    userId,
                    oldPublicId,
                    asset.publicId()
            );

            return;
        }

        log.info(
                "Profile image update completed successfully. userId={}, newAssetId={}",
                userId,
                asset.assetId()
        );
    }


}

