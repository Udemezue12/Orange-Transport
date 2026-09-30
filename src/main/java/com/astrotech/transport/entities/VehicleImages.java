package com.astrotech.transport.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "vehicle_images",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicle_asset",
                        columnNames = {"vehicle_id", "asset_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImages {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "image_hash")
    @Builder.Default
    private String imageHash = null;

    @Column(name = "resource_type")
    @Builder.Default
    private String resourceType = null;
    @Column(name = "image_url")
    @Builder.Default
    private String imageUrl = null;
    @Column(name = "public_id")
    @Builder.Default
    private String publicId = null;
    @Column(name = "asset_id")
    @Builder.Default
    private String assetId = null;
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
}
