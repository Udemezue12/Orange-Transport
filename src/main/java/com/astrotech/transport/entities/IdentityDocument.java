package com.astrotech.transport.entities;

import com.astrotech.transport.enums.DocumentType;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "identity_documents",
        indexes = {
                @Index(name = "idx_identity_docs_document_type", columnList = "document_type"),
                @Index(name = "idx_identity_docs_upload_status", columnList = "upload_status"),
                @Index(name = "idx_identity_docs_verification_status", columnList = "verification_status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(mappedBy = "identityDocument")
    private Profile profile;

    @Column(name = "document_type", length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "document_url", length = 1024)
    @Builder.Default
    private String documentUrl = null;

    @Column(name = "document_hash")
    @Builder.Default
    private String documentHash = null;

    @Column(name = "document_resource_type", length = 50)
    @Builder.Default
    private String documentResourceType = null;

    @Column(name = "document_public_id")
    @Builder.Default
    private String documentPublicId = null;

    @Column(name = "document_asset_id")
    @Builder.Default
    private String documentAssetId  = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", length = 30)
    @Builder.Default
    private ImageUploadStatus uploadStatus = ImageUploadStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 30, nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "status_changed_at")
    private Instant statusChangedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.PENDING;
        }
        if (this.statusChangedAt == null) {
            this.statusChangedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


}


