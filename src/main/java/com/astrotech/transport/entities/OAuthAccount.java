package com.astrotech.transport.entities;


import com.astrotech.transport.OAuth.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_account_provider_subject",
                        columnNames = {"provider", "provider_subject"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_oauth_account_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_oauth_account_provider_subject",
                        columnList = "provider, provider_subject"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_oauth_account_user"
            )
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private OAuthProvider provider;

    @Column(
            name = "provider_subject",
            nullable = false,
            length = 255
    )
    private String providerSubject;

//    @Column(
//            nullable = false,
//            length = 320
//    )
//    private String email;
//
//    @Column(
//            name = "email_verified",
//            nullable = false
//    )
//    private boolean emailVerified;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "last_login_at",
            nullable = false
    )
    private Instant lastLoginAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (lastLoginAt == null) {
            lastLoginAt = createdAt;
        }
    }
}
