package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TerminalRouteRepository extends JpaRepository<TerminalRoute, UUID> {
    Slice<TerminalRoute> findAllByTerminalId(UUID terminalId, Pageable pageable);

    Slice<TerminalRoute> findAllyByRouteId(UUID routeId, Pageable pageable);

    Slice<TerminalRoute> findAllBy(Pageable pageable);

    boolean existsByRouteIdAndTerminalId(
            UUID routeId,
            UUID terminalId
    );

    boolean existsByRouteIdAndTerminalIdAndIdNot(
            UUID routeId,
            UUID terminalId,
            UUID terminalRouteId
    );

    boolean existsByRouteIdAndStopOrder(
            UUID routeId,
            Integer stopOrder
    );

    boolean existsByRouteIdAndStopOrderAndIdNot(
            UUID routeId,
            Integer stopOrder,
            UUID terminalRouteId
    );
}
