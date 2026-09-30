package com.astrotech.transport.entities;

import com.astrotech.transport.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_booking", columnList = "booking_id"),
                @Index(name = "idx_ticket_trip_reservation", columnList = "reservation_id"),
                @Index(name = "idx_ticket_payment", columnList = "payment_id"),
                @Index(name = "idx_ticket_verification_token", columnList = "verification_token")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    @Column(
            name = "verification_token",
            nullable = false,
            unique = true
    )
    private UUID verificationToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingSession bookingSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private TripSeatReservation reservation;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;
    @Column(name = "checked_in")
    private boolean checkedIn;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;
}