package com.astrotech.transport.entities;

import java.math.BigDecimal;

import com.astrotech.transport.enums.VehicleClass;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.UUID;
@Entity
@Table(
        name = "route_fares",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "route_id",
                                "vehicle_class"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteFare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;
    @Column(name = "effective_to", nullable = false)
    private Instant effectiveTo;

    @Column(nullable = false)
    private boolean active;
}
