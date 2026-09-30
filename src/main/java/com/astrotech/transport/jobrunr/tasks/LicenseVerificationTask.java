package com.astrotech.transport.jobrunr.tasks;


import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.enums.LicenseVerificationStatus;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.kyc_verification.dojah.DriverLicenseVerification;
import com.astrotech.transport.service.DriverProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class LicenseVerificationTask {
    private final DriverProfileService driverProfileService;
    private final DriverLicenseVerification driverLicenseVerification;


    @Job(name = "dojah-driver-license-verification", retries = 3)
    public void verifyDriverLicense(String userId, String licenseNumber) {

        log.info("Starting driver's license verification. userId={}, licenseNumber={}",
                userId, licenseNumber);

        try {

            var license = driverLicenseVerification.restClientVerifyLicence(licenseNumber);

            if (license == null || license.entity() == null) {
                log.warn("License verification returned no data. userId={}, licenseNumber={}",
                        userId, licenseNumber);

                throw new IllegalStateException("License verification service returned no data.");
            }

            var profile = driverProfileService.getDriverProfile(UUID.fromString(userId));

            boolean matches = matches(
                    profile.getUser().getFullName(),
                    license.entity().firstName(),
                    license.entity().lastName()
            );

            if (!matches) {

                driverProfileService.updateLicenseVerificationStatus(
                        profile,
                        false,
                        LicenseVerificationStatus.REJECTED
                );

                return;
            }

            driverProfileService.updateLicenseVerificationStatus(
                    profile,
                    true,
                    LicenseVerificationStatus.VERIFIED
            );

            log.info("Driver license verified successfully. userId={}", userId);

        } catch (BadRequestException ex) {


            log.warn("License verification rejected. userId={}, reason={}",
                    userId, ex.getMessage());

            throw ex;

        } catch (Exception ex) {


            log.error("License verification failed for userId={}", userId, ex);

            throw new RuntimeException(
                    "Unable to verify driver's license.",
                    ex
            );
        }
    }
    private boolean matches(String profileName,
                           String firstName,
                           String lastName) {

        profileName = normalize(profileName);

        return profileName.equals(normalize(firstName + " " + lastName))
                || profileName.equals(normalize(lastName + " " + firstName));
    }

    private String normalize(String value) {
        return TrimWhiteSpace
                .trimWhiteSpaceWithUpperCase(value, false);
    }
}
