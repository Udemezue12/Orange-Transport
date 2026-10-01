package com.astrotech.transport.repositories;

import com.astrotech.transport.dto.response.UnAssignedWorkerResponse;
import com.astrotech.transport.entities.Profile;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);

   

    Optional<Profile> findByIdentityDocumentId(UUID identityDocumentId);

    Slice<Profile> findAllBy(Pageable pageable);

    boolean existsByProfilePicAssetId(String assetId);

    @Query("""
                SELECT p
                FROM Profile p
                JOIN FETCH p.user u
                WHERE u.id = :userId
                  AND u.status = :userStatus
                  AND u.role = :role
            """)
    Optional<Profile> findByUserIdAndUserStatusAndRole(
            @Param("userId") UUID userId,
            @Param("userStatus") UserStatus userStatus,
            @Param("role") UserRole role
    );

    @Query("""
                SELECT p
                FROM Profile p
                JOIN FETCH p.user u
                WHERE u.status = :status
                  AND (
                        LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                     OR u.phoneNumber LIKE CONCAT('%', :search, '%')
                  )
            """)
    Slice<Profile> searchProfiles(
            @Param("search") String search,
            @Param("status") UserStatus status,
            Pageable pageable
    );
    @Query("""
                SELECT new com.astrotech.transport.dto.response.UnAssignedWorkerResponse(
                    u.id,
                    u.fullName,
                    u.email,
                    p.id
                )
                FROM Profile p
                JOIN p.user u
                WHERE u.status = :status
                  AND u.role = :role
                  AND NOT EXISTS (
                      SELECT 1
                      FROM u.assignments wa
                      WHERE wa.assignStatus = :activeStatus
                  )
            """)
    Slice<UnAssignedWorkerResponse> findUnassignedWorkers(
            @Param("status") UserStatus status,
            @Param("role") UserRole role,
            @Param("activeStatus") AssignStatus activeStatus,
            Pageable pageable
    );

}
