package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Route;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {
    boolean existsByOriginTerminalIdAndDestinationTerminalId(UUID originTerminalId, UUID destinationTerminalId);

    Slice<Route> findAllBy(Pageable pageable);
}
