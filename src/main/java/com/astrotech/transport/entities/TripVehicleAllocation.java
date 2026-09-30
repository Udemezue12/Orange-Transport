package com.astrotech.transport.entities;

import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "trip_vehicle_allocations",
        indexes = {
                @Index(
                        name = "idx_tva_trip",
                        columnList = "trip_id"
                ),
                @Index(
                        name = "idx_tva_vehicle",
                        columnList = "vehicle_id"
                ),
                @Index(
                        name = "idx_tva_driver",
                        columnList = "driver_profile_id"
                ),
                @Index(
                        name = "idx_tva_trip_status",
                        columnList = "trip_id, allocation_status"
                ),
                @Index(
                        name = "idx_tva_trip_role_status",
                        columnList = "trip_id, allocation_role, allocation_status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripVehicleAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_profile_id", nullable = false)
    private DriverProfile driverProfile;


    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_role", nullable = false, length = 20)
    private AllocationRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_status", nullable = false, length = 20)
    private AllocationStatus status;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "released_at")
    private Instant releasedAt;
}


