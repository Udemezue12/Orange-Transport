package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, UUID> {
    Optional<IdentityDocument> findByProfileId(UUID profileId);
}
