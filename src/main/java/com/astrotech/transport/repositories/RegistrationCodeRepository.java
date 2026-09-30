package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.RegistrationCode;
import com.astrotech.transport.enums.CodeStatus;
import com.astrotech.transport.enums.UserRole;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public interface RegistrationCodeRepository extends JpaRepository<RegistrationCode, String> {


    boolean existsByRegisterCodeAndRole(String registerCode, UserRole role);

    @Modifying
    void deleteByStatus(CodeStatus status);

    Slice<RegistrationCode> findByGeneratedAtAfter(Instant startTimestamp, Pageable pageable);

    @Query("SELECT r FROM RegistrationCode r WHERE r.registerCode = :registerCode AND r.role = :userRole AND r.status = :codeStatus")
    Optional<RegistrationCode> findByIdAndRoleAndStatus(
            @Param("registerCode") String registerCode,
            @Param("codeStatus") CodeStatus codeStatus,
            @Param("userRole") UserRole userRole
    );


    Slice<RegistrationCode> findAllBy(Pageable pageable);
}
