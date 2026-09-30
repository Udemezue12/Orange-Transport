package com.astrotech.transport.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "terminal_routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_terminal_route",
                        columnNames = {"terminal_id", "route_id"}
                ),
                @UniqueConstraint(
                        name = "uk_route_stop_order",
                        columnNames = {"route_id", "stop_order"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "stop_order")
    private Integer stopOrder;

    @Column(name = "stop_order_created_at")
    private Instant stopOrderCreatedAt;
}