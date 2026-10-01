package com.astrotech.transport.repositories;

import com.astrotech.transport.dto.response.UnAssignedWorkerResponse;
import com.astrotech.transport.entities.DriverProfile;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByAssetId(String imageUrl);

    Slice<DriverProfile> findAllBy(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
            SELECT dp
            FROM DriverProfile dp
            WHERE dp.id = :driverProfileId
            """)
    Optional<DriverProfile> findByIdForUpdate(@Param("driverProfileId") UUID driverProfileId);

    Optional<DriverProfile> findByIdAndLicenseVerifiedTrueAndActiveTrue(UUID id);

    @Query("""
                SELECT new com.astrotech.transport.dto.response.UnAssignedWorkerResponse(
                    u.id,
                    u.fullName,
                    u.email,
                    dp.id
                )
                FROM DriverProfile dp
                JOIN dp.user u
                WHERE u.status = :status
                  AND u.role = :role
                  AND NOT EXISTS (
                      SELECT 1
                      FROM u.assignments wa
                      WHERE wa.assignStatus = :activeStatus
                  )
            """)
    Slice<UnAssignedWorkerResponse> findUnassignedDrivers(
            @Param("status") UserStatus status,
            @Param("role") UserRole role,
            @Param("activeStatus") AssignStatus activeStatus,
            Pageable pageable
    );

}
