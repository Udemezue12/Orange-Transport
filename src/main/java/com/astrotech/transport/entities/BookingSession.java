package com.astrotech.transport.entities;



import com.astrotech.transport.enums.BookingSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "booking_sessions",
        indexes = {
                @Index(name = "idx_booking_session_reference", columnList = "reference"),
                @Index(name = "idx_booking_session_status", columnList = "status"),
                @Index(name = "idx_booking_session_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reference", nullable = false, unique = true, length = 30)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private User passenger;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingSessionStatus status;

    @Column(name = "total_amount",nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;



    @OneToMany(
            mappedBy = "bookingSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<TripSeatReservation> reservations = new ArrayList<>();
    @OneToMany(mappedBy = "bookingSession")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
    @OneToMany(
            mappedBy = "bookingSession",
            fetch = FetchType.LAZY

    )
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
