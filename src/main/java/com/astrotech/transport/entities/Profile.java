package com.astrotech.transport.entities;

import com.astrotech.transport.enums.ImageUploadStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(
        name = "profiles",
        indexes = {
                @Index(name = "idx_profiles_user_id", columnList = "user_id"),
                @Index(name = "idx_profiles_identity_doc_id", columnList = "identity_document_id"),
                @Index(name = "idx_profiles_pic_upload_status", columnList = "profile_pic_upload_status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "profile_pic_url")
    @Builder.Default
    private String profilePicUrl = null;

    @Column(name = "profile_pic_hash")
    @Builder.Default
    private String profilePicHash = null;

    @Column(name = "profile_pic_resource_type")
    @Builder.Default
    private String profilePicResourceType = null;

    @Column(name = "profile_pic_public_id")
    @Builder.Default
    private String profilePicPublicId = null;

    @Column(name = "profile_pic_asset_id")
    @Builder.Default
    private String profilePicAssetId = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_pic_upload_status", length = 30)
    @Builder.Default
    private ImageUploadStatus profilePicUploadStatus = ImageUploadStatus.PENDING;



    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_profiles_user")
    )
    private User user;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
            name = "identity_document_id",
            referencedColumnName = "id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_profiles_identity_document")
    )
    private IdentityDocument identityDocument;




}