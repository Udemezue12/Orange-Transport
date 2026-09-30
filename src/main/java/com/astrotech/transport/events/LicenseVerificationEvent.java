package com.astrotech.transport.events;

import com.astrotech.transport.enums.LicenseVerificationStatus;

public record LicenseVerificationEvent(
        String userId,
        String licenseNumber
) {
}
