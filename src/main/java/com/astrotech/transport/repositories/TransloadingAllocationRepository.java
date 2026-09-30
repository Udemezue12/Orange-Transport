package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.TransloadingAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransloadingAllocationRepository extends JpaRepository<TransloadingAllocation, UUID> {


    Optional<TransloadingAllocation> findByTransloadingEventId(UUID eventId);
}
