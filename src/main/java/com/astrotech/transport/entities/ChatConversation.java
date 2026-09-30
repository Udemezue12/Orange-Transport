package com.astrotech.transport.entities;

import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chat_conversations",
        indexes = {
                @Index(name = "idx_chat_conversation_passenger", columnList = "passenger_id"),
                @Index(name = "idx_chat_conversation_agent", columnList = "agent_id"),
                @Index(name = "idx_chat_conversation_status", columnList = "status"),
                @Index(name = "idx_chat_conversation_updated", columnList = "updated_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ConversationStatus status;
    @Column(name = "encrypted")
    @Builder.Default
    private boolean encrypted = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
