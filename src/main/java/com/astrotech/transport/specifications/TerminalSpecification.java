package com.astrotech.transport.specifications;

import com.astrotech.transport.entities.Terminal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerminalSpecification {
    static Specification<Terminal> hasAnyField(String name, String address, UUID supervisorId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("name"), name));
            }
            if (address != null && !address.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("address"), address));
            }
            if (supervisorId != null) {
                predicates.add(cb.equal(root.get("terminalSupervisor").get("id"), supervisorId));
            }


            if (predicates.isEmpty()) {
                return cb.conjunction();
            }


            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
