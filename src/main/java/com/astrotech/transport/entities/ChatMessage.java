package com.astrotech.transport.entities;

import com.astrotech.transport.enums.ChatMessageType;
import com.astrotech.transport.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_message_conversation_created",
                        columnList = "conversation_id, created_at"),
                @Index(name = "idx_chat_message_sender",
                        columnList = "sender_id"),
                @Index(
                        name = "idx_chat_message_conversation_sender_created",
                        columnList = "conversation_id, sender_id, created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatMessageType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status;


    @Column(name = "content_iv", nullable = false)
    private String contentIv;
    @Column(name = "cipher_text", nullable = false, columnDefinition = "TEXT")
    private String cipherText;
    @Column(name = "sent", nullable = false)
    @Builder.Default
    private boolean sent = true;
    @Column(name = "delivered")
    private boolean delivered;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;
    @Column(name = "delivered_at", nullable = true, updatable = false)
    private Instant deliveredAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}



