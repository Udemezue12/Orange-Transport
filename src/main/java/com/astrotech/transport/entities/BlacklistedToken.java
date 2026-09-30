package com.astrotech.transport.entities;


import java.time.OffsetDateTime;
import java.util.UUID;



import com.astrotech.transport.enums.JwtType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "blacklisted_tokens",
        indexes = {
                @Index(name = "idx_blacklisted_jti", columnList = "jti")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private JwtType tokenType;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private OffsetDateTime revokedAt;

    @PrePersist
    public void prePersist() {
        revokedAt = OffsetDateTime.now();
    }
}