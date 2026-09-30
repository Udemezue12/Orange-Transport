package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.request.DriverProfileRequest;
import com.astrotech.transport.dto.response.DriverProfileResponse;
import com.astrotech.transport.entities.DriverProfile;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.LicenseVerificationStatus;

public class DriverProfileMapper {
    public static DriverProfile create(DriverProfileRequest request, User user){
        var trimmedLicenseNumber = TrimWhiteSpace.trimWhiteSpace(request.licenseNumber());
        return DriverProfile.builder()
                .licenseVerified(false)
                .licenseNumber(trimmedLicenseNumber)
                .licenseVerificationStatus(LicenseVerificationStatus.PENDING
                )
                .uploadStatus(ImageUploadStatus.PENDING)
                .active(false)
                .user(user)
                .build();
    }

    public static DriverProfileResponse response(DriverProfile driverProfile){
        var imageUrl = driverProfile.getImageUrl() != null ? driverProfile.getImageUrl() : null;
        var publicId = driverProfile.getPublicId() != null ? driverProfile.getPublicId() : null;
        var assetId = driverProfile.getAssetId() != null ? driverProfile.getAssetId() : null;
        var resourceType = driverProfile.getResourceType() != null ? driverProfile.getResourceType() : null;
        return new DriverProfileResponse(
                driverProfile.getId(),
                driverProfile.getUser().getId(),
                driverProfile.getUser().getFullName(),
                driverProfile.getUser().getEmail(),
                driverProfile.getUser().getPhoneNumber(),
                imageUrl,
                resourceType,
                publicId,
                assetId,
                driverProfile.getLicenseNumber(),
                driverProfile.isLicenseVerified(),
                driverProfile.isActive()
        );
    }
}
