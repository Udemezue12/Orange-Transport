package com.astrotech.transport.jobrunr.tasks;

import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.enums.ImageUploadStatus;

import com.astrotech.transport.enums.ResourceType;
import com.astrotech.transport.service.DriverProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class DriverProfileUploadTask {

    private final DriverProfileService driverProfileService;

    private final CloudinaryService cloudinaryService;


    @Job(name = "profile-image-uploads", retries = 3)
    public void uploadProfileImage(
            String userId,
            String publicId,
            String assetId
    ) {
        log.info(
                "Starting Cloudinary profile image verification. userId={}, assetId={}",
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
                        "Failed to delete invalid profile image from Cloudinary. " +
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
                    "Cloudinary sent profile image rejected. " +
                            "userId={}, expectedResourceType={}, actualResourceType={}",
                    userId,
                    expectedResourceType,
                    responseResourceType
            );

            return;
        }

        driverProfileService.updateProfileImage(
                userUuid,
                asset.publicId(),
                asset.assetId(),
                asset.secureUrl(),
                responseResourceType,
                ImageUploadStatus.VERIFIED
        );

        log.info(
                "Cloudinary profile image verification completed. userId={}, assetId={}",
                userId,
                asset.assetId()
        );
    }


    @Job(name = "profile-image-update-uploads", retries = 3)
    public void updateProfileImage(
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

        // 2. Reject and clean up the new asset if it isn't an image.
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

            driverProfileService.updateProfileImage(
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


        driverProfileService.updateProfileImage(
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