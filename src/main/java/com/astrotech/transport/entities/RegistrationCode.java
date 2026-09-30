package com.astrotech.transport.entities;


import com.astrotech.transport.enums.CodeStatus;
import com.astrotech.transport.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "registration_codes",
        indexes = {
                @Index(name = "idx_registration_codes_code_role", columnList = "register_code, code_role"),
                @Index(name = "idx_registration_codes_generated_at", columnList = "generated_at"),
                @Index(name = "idx_registration_codes_code_role_status", columnList = "register_code, code_role, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationCode {

    @Id
    @Column(name = "register_code", nullable = false)
    private String registerCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_role", nullable = false, length = 30)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CodeStatus status;

    @Builder.Default
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt = Instant.now();
}