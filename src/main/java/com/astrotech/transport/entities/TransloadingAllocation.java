package com.astrotech.transport.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;



@Entity
@Table(
        name = "transloading_allocations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_tpa_event_allocation",
                        columnNames = {"transloading_event_id", "rescue_allocation_id"}
                )
        },
        indexes = {

                @Index(name = "idx_tpa_allocation", columnList = "rescue_allocation_id"),
                @Index(name = "idx_tpa_event", columnList = "transloading_event_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransloadingAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transloading_event_id", nullable = false)
    private TransloadingEvent transloadingEvent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rescue_allocation_id", nullable = false)
    private TripVehicleAllocation rescueAllocation;

    @Column(name = "transloaded_at", nullable = false)
    private Instant transloadedAt;
}

