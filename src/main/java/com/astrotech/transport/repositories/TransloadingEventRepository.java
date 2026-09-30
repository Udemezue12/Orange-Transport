package com.astrotech.transport.repositories;


import com.astrotech.transport.entities.TransloadingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransloadingEventRepository extends JpaRepository<TransloadingEvent,  UUID> {
}
