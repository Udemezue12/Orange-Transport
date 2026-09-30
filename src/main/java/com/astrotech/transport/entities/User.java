package com.astrotech.transport.entities;


import com.astrotech.transport.OAuth.OAuthProvider;

import com.astrotech.transport.enums.OnlineStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_status_role", columnList = "status, role"),
                @Index(name = "idx_users_status_id", columnList = "status, id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_account",
                        columnNames = {"oauth_provider", "oauth_subject"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    @Builder.Default
    private String phoneNumber = null;
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    private OAuthProvider oauthProvider; //remove

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_status")
    private OnlineStatus onlineStatus;


    @Column(name = "suspended")
    @Builder.Default
    private boolean suspended = false;
    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;
    @Column(name = "verified")
    @Builder.Default
    private boolean verified = false;
    @Column(name = "oauth_subject")
    private String oauthSubject; //remove
    @Column(name = "last_seen")
    private Instant lastSeen;
    @Column(name = "verified_at")
    private Instant verifiedAt;
    @Column(name = "deleted_at")
    private Instant deletedAt;
    @Column(name = "suspended_at")
    private Instant suspendedAt;
    @Column(name = "login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "payer",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
    @OneToMany(
            mappedBy = "passenger",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<OAuthAccount> oauthAccounts = new HashSet<>();
    @OneToMany(mappedBy = "worker", fetch = FetchType.LAZY)
    private List<AssignedWorker> assignments = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
