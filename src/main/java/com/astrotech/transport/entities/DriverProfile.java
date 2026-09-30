package com.astrotech.transport.entities;

import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.LicenseVerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "driver_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_driver_profile_license_number",
                        columnNames = "license_number"
                ),
                @UniqueConstraint(
                        name = "uk_driver_profile_asset_id",
                        columnNames = "asset_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = false;

    @Column(name = "image_url")
    @Builder.Default
    private String imageUrl = null;

    @Column(name = "image_hash")
    @Builder.Default
    private String imageHash = null;

    @Column(name = "resource_type")
    @Builder.Default
    private String resourceType = null;

    @Column(name = "public_id")
    @Builder.Default
    private String publicId = null;
    @Column(name = "asset_id")
    @Builder.Default
    private String assetId = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_verification_status", nullable = false)
    private LicenseVerificationStatus licenseVerificationStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "image_upload_status", nullable = false)
    private ImageUploadStatus uploadStatus;


    @Column(name = "license_verified")
    @Builder.Default
    private boolean licenseVerified = false;

    @Column(name = "license_verified_at")
    private Instant licenseVerifiedAt;

}
