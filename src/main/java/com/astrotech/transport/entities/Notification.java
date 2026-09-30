package com.astrotech.transport.entities;

import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user_created",
                        columnList = "user_id, created_at"),
                @Index(name = "idx_notification_user_read",
                        columnList = "user_id, is_read"),
                @Index(name = "idx_notification_type",
                        columnList = "type")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;


    private String actionUrl;


    private UUID referenceId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationReferenceType referenceType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant readAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
