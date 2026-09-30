package com.astrotech.transport.entities;

import com.astrotech.transport.enums.TransloadingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "transloading_events",
        indexes = {
        @Index(name = "idx_tle_location_name", columnList = "location_name"),
        @Index(name = "idx_tle_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransloadingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "incident_reason", nullable = false)
    private String incidentReason;

    @Column(name = "incident_description")
    private String incidentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransloadingStatus status;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;


    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // --- Relationships ---
    @Builder.Default
    @OneToMany(mappedBy = "transloadingEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransloadingAllocation> transloadingAllocations = new ArrayList<>();
}
