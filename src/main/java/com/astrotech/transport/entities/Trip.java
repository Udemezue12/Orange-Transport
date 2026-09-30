package com.astrotech.transport.entities;

import com.astrotech.transport.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Entity
@Table(
        name = "trips",
        indexes = {
                @Index(name = "idx_trips_route_scheduled_departure", columnList = "route_id, scheduled_departure_time"),
                @Index(name = "idx_trips_status", columnList = "status"),
                @Index(name = "idx_trips_code", columnList = "trip_code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trip_code", nullable = false, unique = true)
    private String tripCode;


    @Column(name = "scheduled_departure_time", nullable = false)
    private Instant scheduledDepartureTime;

    @Column(name = "scheduled_arrival_time", nullable = false)
    private Instant scheduledArrivalTime;

    @Column(name = "boarding_time", nullable = false)
    private Instant boardingTime;

    @Column(name = "booking_cutoff", nullable = false)
    private Instant bookingCutoff;


    @Column(name = "actual_departure_time")
    private Instant actualDepartureTime;

    @Column(name = "actual_arrival_time")
    private Instant actualArrivalTime;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatus status;

    @Column(name = "delay_reason")
    @Builder.Default
    private String delayReason = null;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id")
    private DriverProfile driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_loader_profile_id")
    private Profile vehicleLoaderProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<TripSeatReservation> seatReservations = new ArrayList<>();



    @Version
    private Long version;
}