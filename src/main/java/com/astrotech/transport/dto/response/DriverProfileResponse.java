package com.astrotech.transport.dto.response;

import java.util.UUID;

public record DriverProfileResponse(
        UUID driverProfileId,
        UUID userId,
        String fullName,
        String email,
        String phoneNumber,
        String imageUrl,
        String resourceType,
        String publicId,
        String assetId,
        String licenseNumber,
        Boolean licenseVerified,
        Boolean active

) {
}
