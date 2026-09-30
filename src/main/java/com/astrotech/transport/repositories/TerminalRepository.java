package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Terminal;
import com.astrotech.transport.projection.ExistingTerminalFields;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID>, JpaSpecificationExecutor<Terminal> {



    @Query("""
    SELECT
        t.name AS name,
        t.address AS address,
        t.terminalSupervisor.id AS supervisorId
    FROM Terminal t
    WHERE
           t.name = :name
        OR t.address = :address
        OR t.terminalSupervisor.id = :supervisorId
    """)
    List<ExistingTerminalFields> findExistingFields(
            String name,
            String address,
            UUID supervisorId
    );

    boolean existsByName(String name);

    boolean existsByCity(String city);

    boolean existsByState(String state);

    boolean existsByAddress(String address);

    boolean existsByTerminalSupervisorIdAndIdNot(UUID supervisorId, UUID terminalId);

    Slice<Terminal> findAllBy(Pageable pageable);

    Optional<Terminal> findByIdAndTerminalSupervisorId(UUID terminalId, UUID userId);
}
