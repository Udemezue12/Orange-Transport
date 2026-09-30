package com.astrotech.transport.jobrunr.tasks;

import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.enums.ResourceType;
import com.astrotech.transport.events.VehicleImageReplacement;
import com.astrotech.transport.service.VehicleImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class VehicleImageUploadTask {

    private final VehicleImageService vehicleImageService;
    private final CloudinaryService cloudinaryService;




    @Job(name = "vehicle-image-uploads", retries = 3)
    public void uploadVehicleImage(
            String vehicleId,
            String publicId,
            String assetId
    ) {
        log.info(
                "Starting Cloudinary vehicle image verification. vehicleId={}, assetId={}",
                vehicleId,
                assetId
        );

        var vehicleUuid = UUID.fromString(vehicleId);

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
                        "Failed to delete invalid vehicle image from Cloudinary. " +
                                "vehicleId={}, assetId={}, publicId={}, resourceType={}",
                        vehicleId,
                        asset.assetId(),
                        asset.publicId(),
                        responseResourceType
                );
            }

            vehicleImageService.saveVehicleImage(
                    vehicleUuid,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            log.warn(
                    "Cloudinary vehicle image rejected. " +
                            "vehicleId={}, expectedResourceType={}, actualResourceType={}",
                    vehicleId,
                    expectedResourceType,
                    responseResourceType
            );

            return;
        }

        vehicleImageService.saveVehicleImage(
                vehicleUuid,
                asset.assetId(),
                asset.publicId(),
                responseResourceType,
                asset.thumbNailUrl(),
                asset.secureUrl()
        );

        log.info(
                "Cloudinary vehicle image verification completed. vehicleId={}, assetId={}",
                vehicleId,
                asset.assetId()
        );
    }


    @Job(name = "vehicle-image-update-uploads", retries = 3)
    public void updateVehicleImages(
            UUID vehicleId,
            List<VehicleImageReplacement> replacements
    ) {
        for (var replacement : replacements) {

            var vehicleImage = vehicleImageService.getVehicleImages(
                    replacement.oldImageId(),
                    vehicleId
            );

            if (vehicleImage == null) {
                continue;
            }

            var asset = cloudinaryService.getVerifiedCloudinaryAsset(
                    replacement.newPublicId(),
                    replacement.newAssetId()
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
                            "Failed to delete invalid replacement vehicle image. " +
                                    "vehicleId={}, assetId={}, publicId={}, resourceType={}",
                            vehicleId,
                            asset.assetId(),
                            asset.publicId(),
                            responseResourceType
                    );
                }

                log.warn(
                        "Vehicle image replacement rejected due to invalid resource type. " +
                                "vehicleId={}, oldImageId={}, expectedResourceType={}, actualResourceType={}",
                        vehicleId,
                        replacement.oldImageId(),
                        expectedResourceType,
                        responseResourceType
                );

                continue;
            }

            var deleted = cloudinaryService.deleteResource(
                    vehicleImage.getPublicId(),
                    vehicleImage.getResourceType()
            );

            if (!deleted) {
                throw new IllegalStateException(
                        "Failed to delete old vehicle image: "
                                + vehicleImage.getPublicId()
                );
            }

            vehicleImageService.updateVehicleImage(
                    vehicleImage,
                    asset.assetId(),
                    asset.publicId(),
                    responseResourceType,
                    asset.thumbNailUrl(),
                    asset.secureUrl()
            );
        }
    }





}
