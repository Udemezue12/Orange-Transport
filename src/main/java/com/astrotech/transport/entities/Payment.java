package com.astrotech.transport.entities;

import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_reference", columnList = "generated_reference"),
                @Index(name = "idx_payment_provider_reference", columnList = "payment_provider_reference"),
                @Index(name = "idx_payment_booking", columnList = "booking_id"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "generated_reference", nullable = false, unique = true)
    private String generatedReference;

    @Column(name = "payment_provider_reference", unique = true)
    private String paymentProviderReference;
    @Column(name = "payment_provider_transaction_id", unique = true)
    private String paymentProviderTransactionId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "payment_channel")
    private String paymentChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "refunded_id")
    @Builder.Default
    private String refundId = null;
    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = null;

    @Builder.Default
    @Column(name = "processed")
    private Boolean processed = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id")
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_session_id")
    private BookingSession bookingSession;


    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "failed_at")

    private Instant failedAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name ="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
