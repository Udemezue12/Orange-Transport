package com.astrotech.transport.entities;

import com.astrotech.transport.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "trip_seat_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_seat",
                        columnNames = {"trip_id", "seat_id"}
                )
        },
        indexes = {
                @Index(name = "idx_trip_reservation_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_reservation_status", columnList = "status"),
                @Index(name = "idx_trip_reservation_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "passenger_name" , nullable = false)
    private String passengerName;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_session_id")
    private BookingSession bookingSession;




    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Version
    private Long version;
}
