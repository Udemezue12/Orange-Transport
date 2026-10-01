package com.astrotech.transport.repositories;


import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.AssignedServiceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignedWorkerRepository extends JpaRepository<AssignedWorker, UUID> {

    Optional<AssignedWorker> findByAssignedServiceId(UUID serviceId);

    Optional<AssignedWorker> findByAssignedServiceIdAndAssignedServiceType(UUID serviceId, AssignedServiceType serviceType);

    Slice<AssignedWorker> findAllBy(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE AssignedWorker a SET a.assignStatus = :status WHERE a.assignedServiceId = :serviceId")
    void updateAssignStatusByServiceId(@Param("serviceId") UUID serviceId, @Param("status") AssignStatus status);
}
